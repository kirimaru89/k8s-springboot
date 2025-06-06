package com.example.demo.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Configuration
public class KafkaConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

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

        return factory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        
        // Tracing: Enable observations for the template to preserve trace context
        template.setObservationEnabled(true);
        return template;
    }
}