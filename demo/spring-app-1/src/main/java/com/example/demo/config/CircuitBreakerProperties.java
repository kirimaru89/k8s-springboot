package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "resilience4j.circuitbreaker")
public class CircuitBreakerProperties {
    // This matches directly with your YAML structure
    private Map<String, CircuitBreakerConfigValues> instances;

    @Data
    public static class CircuitBreakerConfigValues {
        private Float failureRateThreshold;
        private Integer slidingWindowSize;
        private Integer minimumNumberOfCalls;
        private Integer permittedNumberOfCallsInHalfOpenState;
        private Integer waitDurationInOpenState;
        private Float slowCallRateThreshold;
        private Integer slowCallDurationThreshold;
        private String slidingWindowType; // "COUNT_BASED" or "TIME_BASED"
        private Boolean automaticTransitionFromOpenToHalfOpenEnabled;
    }
}