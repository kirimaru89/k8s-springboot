package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.EndpointHandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;


@Configuration
public class KafkaConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${kafka.listener.missing-topics-fatal:false}")
    private boolean missingTopicsFatal;
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler defaultErrorHandler) {
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

        factory.setCommonErrorHandler(defaultErrorHandler);

        return factory;
    }

    @Bean
    public RetryTopicConfiguration retryTopicConfiguration(KafkaTemplate<String, String> nonTransactionalKafkaTemplate) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .maxAttempts(5)
                .fixedBackOff(5000)
                .retryTopicSuffix("-retry")
                .dltSuffix("-dlt")
                // happen after message is sent to DLT
                .dltHandlerMethod(new EndpointHandlerMethod("myCustomDltProcessor", "processDltMessage"))
                .create(nonTransactionalKafkaTemplate);
    }

    // @Bean
    // public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
    //     FixedBackOff backOff = new FixedBackOff(5000L, 3);

    //     DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate) {};

    //     DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

    //     errorHandler.addRetryableExceptions(IllegalArgumentException.class);

    //     errorHandler.setRetryListeners((record, ex, attempt) ->
    //         log.warn("🔁 Retry attempt {} failed for record: {}, error: {}", attempt, record.value(), ex.getMessage())
    //     );

    //     return errorHandler;
    // }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        // Enable observations for the template to preserve trace context
        template.setObservationEnabled(true);
        return template;
    }
}