package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.BodyFilter;

@Configuration
public class LogbookConfig {

    @Bean
    public BodyFilter bodyFilter() {
        return (contentType, body) -> {
            String result = body;

            // Mask credit card numbers
            result = result.replaceAll("\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b", "****-****-****-$4");

            // Mask email addresses
            result = result.replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b", "****@$1");

            // Mask sensitive JSON fields
            result = result.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"****\"");
            result = result.replaceAll("\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"****\"");
            result = result.replaceAll("\"creditCard\"\\s*:\\s*\"[^\"]*\"", "\"creditCard\":\"****\"");
            result = result.replaceAll("\"cardNumber\"\\s*:\\s*\"[^\"]*\"", "\"cardNumber\":\"****\"");
            result = result.replaceAll("\"privateKey\"\\s*:\\s*\"[^\"]*\"", "\"privateKey\":\"****\"");
            result = result.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"****\"");
            result = result.replaceAll("\"apiKey\"\\s*:\\s*\"[^\"]*\"", "\"apiKey\":\"****\"");
            result = result.replaceAll("\"ssn\"\\s*:\\s*\"[^\"]*\"", "\"ssn\":\"****\"");

            return result;
        };
    }
}