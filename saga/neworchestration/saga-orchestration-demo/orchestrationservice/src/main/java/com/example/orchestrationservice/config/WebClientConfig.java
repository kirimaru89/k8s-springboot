package com.example.orchestrationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Potentially add default headers or other configurations here if needed across all WebClient instances
        return WebClient.builder();
    }
} 