package com.vietinbank.kproducer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import io.lettuce.core.resource.Delay;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ClientOptions.Builder;
import io.lettuce.core.ClientOptions.DisconnectedBehavior;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public ClientResources lettuceClientResources(ObservationRegistry observationRegistry) {
        MicrometerTracing micrometerTracing = new MicrometerTracing(observationRegistry, "redis", true);
        return ClientResources.builder()
                .tracing(micrometerTracing)
                .reconnectDelay(Delay.constant(Duration.ofSeconds(30)))
                .build();
    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return builder -> builder
            .clientOptions(ClientOptions.builder()
                .pingBeforeActivateConnection(true)
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build());
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get error on key '{}': {}", key, exception.getMessage());
                // Return null to indicate cache miss, allowing the application to proceed
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put error on key '{}': {}", key, exception.getMessage());
                // Skip cache put and continue
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict error on key '{}': {}", key, exception.getMessage());
                // Skip cache evict and continue
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear error: {}", exception.getMessage());
                // Skip cache clear and continue
            }
        };
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        int booksTtl = 10;
        int defaultTtl = 30;

        // Define TTLs per cache name
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("books", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(booksTtl)));

        return RedisCacheManager.builder(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigurations)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(defaultTtl))) // Fallback TTL
            .enableStatistics()
            .build();
    }

    /**
     * Registers cache metrics for monitoring
     */
    @EventListener(ApplicationReadyEvent.class)
    public void bindCacheToRegistry() {
        CacheManager cacheManager = applicationContext.getBean(CacheManager.class);
        CacheMetricsRegistrar cacheMetricsRegistrar = applicationContext.getBean(CacheMetricsRegistrar.class);

        final Cache booksCache = cacheManager.getCache("books");
        cacheMetricsRegistrar.bindCacheToRegistry(booksCache);
    }
}