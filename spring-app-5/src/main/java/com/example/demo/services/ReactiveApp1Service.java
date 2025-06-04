package com.example.demo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ReactiveApp1Service {
    private static final Logger log = LoggerFactory.getLogger(ReactiveApp1Service.class);
    
    @Autowired
    private WebClient webClient;
    
    public String fireAndForgetReactive() {
        // Spring Boot automatically handles trace context propagation
        webClient.get()
                .uri("http://spring-app-5:8081/api/hello")  // Use service name if possible
                .retrieve()
                .bodyToMono(String.class)
                // Just subscribe with handlers
                .subscribe(
                    response -> log.info("Got response: {}", response),
                    error -> log.error("Error calling app1", error)
                );
        
        return "Request initiated via WebClient";
    }
}