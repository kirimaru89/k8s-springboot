package com.example.demo.config;

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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.Duration;
@Configuration
public class RedisConfig implements CachingConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${cache.redis.key-prefix}")
    private String keyPrefix;

    @Value("${cache.redis.time-to-live}")
    private long defaultTtlMs;

    @Bean
    public ClientResources lettuceClientResources(ObservationRegistry observationRegistry) {
        MicrometerTracing micrometerTracing = new MicrometerTracing(observationRegistry, "redis", true);
        return ClientResources.builder()
                .tracing(micrometerTracing)
                .build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(ClientResources lettuceClientResources) {
        ClientOptions clientOptions = ClientOptions.builder()
            .autoReconnect(false)
            .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .clientResources(lettuceClientResources)
            .commandTimeout(Duration.ofMillis(defaultTtlMs))
            .shutdownTimeout(Duration.ZERO) // optional: no wait on shutdown
            .clientOptions(clientOptions)
            .build();

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisUsername != null && !redisUsername.isEmpty()) {
            redisConfig.setUsername(redisUsername);
        }
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    /**
     * Cấu hình RedisTemplate để thao tác với Redis
     * RedisTemplate là một helper class giúp thao tác với Redis dễ dàng hơn
     * 
     * @param connectionFactory Factory để tạo kết nối Redis
     * @return RedisTemplate đã được cấu hình
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Cấu hình Jackson2JsonRedisSerializer để chuyển đổi object thành JSON và ngược lại
        ObjectMapper mapper = new ObjectMapper();
        // Cho phép truy cập vào tất cả các field của object
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // Cho phép lưu thông tin về kiểu dữ liệu khi serialize
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        // Cấu hình serializer cho key và value
        // Key được serialize dưới dạng string
        template.setKeySerializer(new StringRedisSerializer());
        // Value được serialize dưới dạng JSON
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // Hash key và value cũng được serialize tương tự
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * Cấu hình CacheManager để quản lý cache trong ứng dụng
     * CacheManager sẽ sử dụng Redis làm backend để lưu trữ cache
     * 
     * @param connectionFactory Factory để tạo kết nối Redis
     * @return CacheManager đã được cấu hình
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Cấu hình mặc định cho tất cả các cache
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // Thời gian sống mặc định của cache là 10 phút
                .disableCachingNullValues() // Không lưu cache cho các giá trị null
                .prefixCacheNameWith(keyPrefix)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)));

        // Tạo CacheManager với các cấu hình cụ thể cho từng loại cache
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .enableStatistics()
                .build();
    }

    // prevent redis error from stopping the endpoint if using Cacheable annotation
    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }

    // register cache to metrics
    @EventListener(ApplicationReadyEvent.class)
    public void bindCacheToRegistry() {
        CacheManager cacheManager = applicationContext.getBean(CacheManager.class);
        CacheMetricsRegistrar cacheMetricsRegistrar = applicationContext.getBean(CacheMetricsRegistrar.class);

        final Cache booksCache = cacheManager.getCache("books");
        cacheMetricsRegistrar.bindCacheToRegistry(booksCache);
    }
}