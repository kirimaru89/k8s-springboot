package com.example.demo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.ScopedSpan;
import com.example.demo.repositories.BookRepository;
import com.example.demo.models.Book;

@Service
public class KafkaConsumerService {

    @Autowired
    private BookRepository bookRepository;

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private Tracer tracer;
    
    @KafkaListener(topics = "app-communication", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        // Create and start a scoped span (automatically placed on the current thread)
        ScopedSpan span = tracer.startScopedSpan("process-kafka-message");
        try {
            // Add tags/attributes to the span
            span.tag("message.type", "kafka");
            span.tag("kafka.topic", "app-communication");
            
            var books = bookRepository.findAll();
            log.info("Processing message:");
            for (Book book : books) {
                log.info("Processing message: {}", book.getTitle());
            }
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            // Always close the span
            span.end();
        }
    }
}