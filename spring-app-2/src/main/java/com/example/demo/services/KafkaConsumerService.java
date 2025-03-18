package com.example.demo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.tracing.Tracer;
import com.example.demo.repositories.BookRepository;
import com.example.demo.models.Book;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@Service
public class KafkaConsumerService {

    @Autowired
    private BookRepository bookRepository;

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private Tracer tracer;
    
    @WithSpan("KafkaConsumerService.listen")
    @KafkaListener(topics = "app-communication", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        // Log the trace context to verify it's being properly propagated
        // if (tracer.currentSpan() != null) {
        //     log.info("Received message with traceId: {}, spanId: {}", 
        //              tracer.currentSpan().context().traceId(),
        //              tracer.currentSpan().context().spanId());
        // }
        
        // log.info("Processing message: {}", message);

        var books = bookRepository.findAll();
        log.info("Processing message:");
        for (Book book : books) {
            log.info("Processing message: {}", book.getTitle());
        }

        // throw new RuntimeException("Test exception");
    }
}