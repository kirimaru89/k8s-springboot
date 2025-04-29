package com.example.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.CamelContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SagaOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SagaOrchestratorApplication.class, args);
    }
} 