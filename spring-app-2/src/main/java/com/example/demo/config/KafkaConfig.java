package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;


@Configuration
public class KafkaConfig {

    @Value("${kafka.listener.missing-topics-fatal:false}")
    private boolean missingTopicsFatal;
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
            
        factory.setConsumerFactory(consumerFactory);
        
        // This enables trace context extraction from Kafka headers
        factory.getContainerProperties().setObservationEnabled(true);
        
        // CRITICAL: Make missing topics non-fatal to allow application to start
        factory.setMissingTopicsFatal(missingTopicsFatal);
        
        // Set autoStartup to false - we'll start listeners programmatically
        // by KafkaListenerStarter.java
        factory.setAutoStartup(false);

        // 🔥 CRITICAL: Needed for Acknowledgment argument to work
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, String> nonTransactionalKafkaTemplate) {
        return new DeadLetterPublishingRecoverer(nonTransactionalKafkaTemplate,
                (record, ex) -> {
                    // Use Apache Kafka TopicPartition, not Spring's TopicPartitionOffset
                    String dltTopic = "dlt." + record.topic();
                    return new TopicPartition(dltTopic, record.partition());
                });
    }

    @Bean
    public RetryTopicConfiguration retryTopicConfiguration(KafkaTemplate<String, String> nonTransactionalKafkaTemplate) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .maxAttempts(5)
                .fixedBackOff(5000)
                .retryTopicSuffix("-retry")
                .dltSuffix("-dlt")
                .create(nonTransactionalKafkaTemplate);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        // Enable observations for the template to preserve trace context
        template.setObservationEnabled(true);
        return template;
    }

    @Bean
    public KafkaTemplate<String, String> transactionalKafkaTemplate(
        ProducerFactory<String, String> producerFactory) {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true);
        return template;
    }

    @Bean
    @Qualifier("nonTransactionalKafkaTemplate")
    public KafkaTemplate<String, String> nonTransactionalKafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Still use idempotence but NOT transactions
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }
}