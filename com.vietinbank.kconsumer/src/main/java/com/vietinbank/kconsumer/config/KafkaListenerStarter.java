package com.vietinbank.kconsumer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerStarter {
    private static final Logger log = LoggerFactory.getLogger(KafkaListenerStarter.class);
    
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    
    /**
     * Start Kafka listeners after application is fully initialized.
     * This allows the application to start even if Kafka is unavailable.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void startKafkaListeners() {
        log.info("Starting Kafka listeners...");
        
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            try {
                if (!container.isRunning()) {
                    container.start();
                    log.info("Started Kafka listener: {}", container.getListenerId());
                }
            } catch (Exception e) {
                log.error("Failed to start Kafka listener {}: {}", 
                         container.getListenerId(), e.getMessage(), e);
                // Don't rethrow - we want other listeners to try starting
            }
        }
        
        log.info("Kafka listeners startup completed");
    }
}