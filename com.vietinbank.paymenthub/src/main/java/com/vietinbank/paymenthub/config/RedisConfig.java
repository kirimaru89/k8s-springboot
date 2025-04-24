package com.vietinbank.paymenthub.config;

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

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.redis.connection.RedisClusterConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username:}")
    private String redisUsername;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${cache.redis.key-prefix:}")
    private String keyPrefix;

    @Value("${cache.redis.time-to-live:600000}")
    private long defaultTtlMs;

    /**
     * Creates client resources with Micrometer tracing for Redis operations
     */
    @Bean
    public ClientResources lettuceClientResources(ObservationRegistry observationRegistry) {
        MicrometerTracing micrometerTracing = new MicrometerTracing(observationRegistry, "redis", true);
        return ClientResources.builder()
                .tracing(micrometerTracing)
                .build();
    }

    /**
     * Creates a Redis connection factory with tracing support
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(ClientResources lettuceClientResources) {
        // Configure client options
        ClientOptions clientOptions = ClientOptions.builder()
            .autoReconnect(true) // Enable auto-reconnect for better resilience
            .build();

        // Configure Lettuce client
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .clientResources(lettuceClientResources)
            .commandTimeout(Duration.ofMillis(defaultTtlMs))
            .clientOptions(clientOptions)
            .build();

        // Configure Redis connection
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisUsername != null && !redisUsername.isEmpty()) {
            redisConfig.setUsername(redisUsername);
        }

        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        return new LettuceConnectionFactory(redisConfig, clientConfig);
        // Configure Redis cluster connection
        // List<String> clusterNodes = Arrays.asList(
        //     "redis-node1:6379",
        //     "redis-node2:6379",
        //     "redis-node3:6379"
        // );

        // RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(clusterNodes);
        
        // if (redisPassword != null && !redisPassword.isEmpty()) {
        //     clusterConfig.setPassword(redisPassword);
        // }

        // return new LettuceConnectionFactory(clusterConfig, clientConfig);
    }

    /**
     * Creates a RedisTemplate for Redis operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Creates a CacheManager for Spring's caching abstraction
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(defaultTtlMs))
                .disableCachingNullValues();
                
        // Add prefix if specified
        if (keyPrefix != null && !keyPrefix.isEmpty()) {
            config = config.prefixCacheNameWith(keyPrefix);
        }

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .enableStatistics()
                .withCacheConfiguration("books", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("users", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)))
                .build();
    }

    /**
     * Provides error handling for cache operations
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get error on key '{}': {}", key, exception.getMessage());
                // You can handle fallback logic here if needed
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put error on key '{}': {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict error on key '{}': {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear error: {}", exception.getMessage());
            }
        };
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