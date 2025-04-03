package com.example.demo.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;

@Configuration
public class RedisTracingConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

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
            .commandTimeout(Duration.ofSeconds(5)) // fail fast on command timeout
            .shutdownTimeout(Duration.ZERO) // optional: no wait on shutdown
            .clientOptions(clientOptions)
            .build();

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }
}