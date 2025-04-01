package com.example.demo.config;

import io.github.resilience4j.circuitbreaker.*;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;

@Component
public class CircuitBreakerConfigUpdater {

    private final CircuitBreakerProperties properties;
    private final CircuitBreakerRegistry registry;

    public CircuitBreakerConfigUpdater(CircuitBreakerProperties properties, CircuitBreakerRegistry registry) {
        this.properties = properties;
        this.registry = registry;
    }

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh() {
        System.out.println("♻️ Refresh event received — reloading CircuitBreaker configs...");
        reloadAll();
    }

    private void reloadAll() {
        if (properties.getInstances() == null || properties.getInstances().isEmpty()) {
            System.err.println("⚠️ No circuit breaker configs found.");
            return;
        }

        properties.getInstances().forEach((name, cfg) -> {
            try {
                CircuitBreakerConfig.SlidingWindowType windowType =
                        "TIME_BASED".equalsIgnoreCase(cfg.getSlidingWindowType())
                                ? CircuitBreakerConfig.SlidingWindowType.TIME_BASED
                                : CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

                CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                        .failureRateThreshold(cfg.getFailureRateThreshold() != null ? cfg.getFailureRateThreshold() : 50f)
                        .slidingWindowSize(cfg.getSlidingWindowSize() != null ? cfg.getSlidingWindowSize() : 10)
                        .minimumNumberOfCalls(cfg.getMinimumNumberOfCalls() != null ? cfg.getMinimumNumberOfCalls() : 5)
                        .waitDurationInOpenState(Duration.ofMillis(cfg.getWaitDurationInOpenState() != null ? cfg.getWaitDurationInOpenState() : 10000))
                        .permittedNumberOfCallsInHalfOpenState(cfg.getPermittedNumberOfCallsInHalfOpenState() != null ? cfg.getPermittedNumberOfCallsInHalfOpenState() : 3)
                        .slowCallRateThreshold(cfg.getSlowCallRateThreshold() != null ? cfg.getSlowCallRateThreshold() : 100f)
                        .slowCallDurationThreshold(Duration.ofMillis(cfg.getSlowCallDurationThreshold() != null ? cfg.getSlowCallDurationThreshold() : 2000))
                        .slidingWindowType(windowType)
                        .automaticTransitionFromOpenToHalfOpenEnabled(Boolean.TRUE.equals(cfg.getAutomaticTransitionFromOpenToHalfOpenEnabled()))
                        .build();

                registry.replace(name, CircuitBreaker.of(name, config));
                System.out.printf("✅ CircuitBreaker [%s] updated dynamically.%n", name);
                
                // get CircuitBreaker by name
                CircuitBreaker circuitBreaker = registry.circuitBreaker(name);
                System.out.println("🔍 Current CircuitBreaker configs:");
                System.out.println(circuitBreaker.getCircuitBreakerConfig());

                System.out.println("--------end--------");

            } catch (Exception e) {
                System.err.printf("❌ Error configuring CircuitBreaker [%s]: %s%n", name, e.getMessage());
                e.printStackTrace();
            }
        });
    }
}