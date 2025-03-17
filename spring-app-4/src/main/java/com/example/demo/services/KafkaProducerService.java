package com.example.demo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "app-communication";
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void sendMessage(String message) {
        log.info("Sending message to Kafka: {}", message);
        
        kafkaTemplate.send(TOPIC, message)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message sent to topic {} with offset {}", 
                        TOPIC, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send message to Kafka", ex);
                }
            });
    }
}