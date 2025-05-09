package com.example.orchestrationservice.config;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

    @Bean(name = "camelContextConfiguration")
    public CamelContextConfiguration camelContextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext context) {
                context.setTracing(true);
                context.setMessageHistory(true);
            }

            @Override
            public void afterApplicationStart(CamelContext context) {
                // No additional configuration needed after start
            }
        };
    }

    @Bean
    public ProducerTemplate producerTemplate(CamelContext camelContext) {
        return camelContext.createProducerTemplate();
    }
} 