package com.example.demo.config;

import java.time.Duration;

import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

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
            // System.err.println("⚠️ No circuit breaker configs found.");
            return;
        }

        properties.getInstances().forEach((name, cfg) -> {
            try {
                CircuitBreakerConfig.SlidingWindowType windowType =
                        "TIME_BASED".equalsIgnoreCase(cfg.getSlidingWindowType())
                                ? CircuitBreakerConfig.SlidingWindowType.TIME_BASED
                                : CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

                CircuitBreakerConfig newConfig = CircuitBreakerConfig.custom()
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

                CircuitBreaker existingBreaker = registry.circuitBreaker(name);
                CircuitBreakerConfig existingConfig = existingBreaker.getCircuitBreakerConfig();
                
                if (!configsEqual(existingConfig, newConfig)) {
                    registry.replace(name, CircuitBreaker.of(name, newConfig));
                    // System.out.printf("✅ CircuitBreaker [%s] updated dynamically.%n", name);
                } else {
                    // System.out.printf("ℹ️ CircuitBreaker [%s] config unchanged, skipping update.%n", name);
                }
                
                // get CircuitBreaker by name
                CircuitBreaker circuitBreaker = registry.circuitBreaker(name);
                // System.out.println("🔍 Current CircuitBreaker configs:");
                // System.out.println(circuitBreaker.getCircuitBreakerConfig());

                // System.out.println("--------end--------");

            } catch (Exception e) {
                // System.err.printf("❌ Error configuring CircuitBreaker [%s]: %s%n", name, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private boolean configsEqual(CircuitBreakerConfig config1, CircuitBreakerConfig config2) {
        return config1.getFailureRateThreshold() == config2.getFailureRateThreshold() &&
               config1.getSlidingWindowSize() == config2.getSlidingWindowSize() &&
               config1.getMinimumNumberOfCalls() == config2.getMinimumNumberOfCalls() &&
               config1.getWaitIntervalFunctionInOpenState() == config2.getWaitIntervalFunctionInOpenState() &&
               config1.getPermittedNumberOfCallsInHalfOpenState() == config2.getPermittedNumberOfCallsInHalfOpenState() &&
               config1.getSlowCallRateThreshold() == config2.getSlowCallRateThreshold() &&
               config1.getSlowCallDurationThreshold().toMillis() == config2.getSlowCallDurationThreshold().toMillis() &&
               config1.getSlidingWindowType() == config2.getSlidingWindowType() &&
               config1.isAutomaticTransitionFromOpenToHalfOpenEnabled() == config2.isAutomaticTransitionFromOpenToHalfOpenEnabled();
    }
}