package com.example.orchestrationservice.config;
import org.apache.camel.saga.InMemorySagaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaConfig {
    @Bean
    public InMemorySagaService inMemorySagaService() {
        return new InMemorySagaService();
    }
}
