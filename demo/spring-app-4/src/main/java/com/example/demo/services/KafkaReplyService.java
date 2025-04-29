package com.example.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j
public class KafkaReplyService {
    private static final Logger log = LoggerFactory.getLogger(KafkaReplyService.class);
    private static final String REQUEST_TOPIC = "kRequests";
    
    @Autowired
    private KafkaTemplate<String, String> replyTemplate;
    
    @KafkaListener(topics = REQUEST_TOPIC, groupId = "spring-app-4-group")
    public void listen(String request, @Header(KafkaHeaders.REPLY_TOPIC) String replyTopic,
                      @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        log.info("Received request: {}", request);
        log.info("correlationId: {}", correlationId);
        
        // Process the request and generate reply
        String reply = processRequest(request);
        
        // Create a message with the correlation ID
        MessageBuilder<String> messageBuilder = MessageBuilder
            .withPayload(reply)
            .setHeader(KafkaHeaders.TOPIC, replyTopic)
            .setHeader(KafkaHeaders.CORRELATION_ID, correlationId);
            
        // Send the reply
        replyTemplate.send(messageBuilder.build())
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Reply sent successfully with correlation ID: {}", correlationId);
                } else {
                    log.error("Failed to send reply", ex);
                }
            });
    }
    
    private String processRequest(String request) {
        // Add your business logic here
        return "Processed: " + request;
    }
} 