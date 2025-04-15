package com.example.demo.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.BackOff;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:kafka:9092}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id:spring-app-1}")
    private String groupId;
    
    // Dead Letter Topic prefix
    private static final String DLT_PREFIX = "dlt.";

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Enable at least once processing
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // Limit max poll records for better error handling
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, String> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> {
                    // Use Apache Kafka TopicPartition, not Spring's TopicPartitionOffset
                    String dltTopic = "dlt." + record.topic();
                    return new TopicPartition(dltTopic, record.partition());
                });
    }

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        // Configure retry with exponential backoff
        BackOff exponentialBackOff = new ExponentialBackOffWithMaxRetries(5);
        ((ExponentialBackOffWithMaxRetries) exponentialBackOff).setInitialInterval(1_000L);
        ((ExponentialBackOffWithMaxRetries) exponentialBackOff).setMultiplier(2.0);
        ((ExponentialBackOffWithMaxRetries) exponentialBackOff).setMaxInterval(10_000L);
        
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                deadLetterPublishingRecoverer, 
                exponentialBackOff);
        
        // Fail fast on non-recoverable errors (no retries)
        errorHandler.addNotRetryableExceptions(
                org.apache.kafka.common.errors.InvalidTopicException.class,
                org.apache.kafka.common.errors.RecordTooLargeException.class,
                org.apache.kafka.common.errors.SerializationException.class);
        
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
            
        factory.setConsumerFactory(consumerFactory());
        // Set up error handling
        factory.setCommonErrorHandler(errorHandler);
        // Configure acknowledgment mode to manual
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // This enables trace context extraction from Kafka headers
        factory.getContainerProperties().setObservationEnabled(true);

        return factory;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Enable idempotence to prevent duplicate messages during retries
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // Required for idempotence
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        // Configure retries
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        // Prevent message reordering during retries
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory());
        // Enable observations for the template to preserve trace context
        template.setObservationEnabled(true);
        return template;
    }
    
    @Bean
    public CircuitBreaker kafkaCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(5)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build();
        
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        return registry.circuitBreaker("kafkaCircuitBreaker");
    }
}