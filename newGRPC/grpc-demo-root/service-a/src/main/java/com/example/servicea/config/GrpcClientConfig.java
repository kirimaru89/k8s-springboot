package com.example.servicea.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micrometer.core.instrument.binder.grpc.ObservationGrpcClientInterceptor;
import io.micrometer.observation.ObservationRegistry;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.service-b.address}")
    private String serviceBAddress;

    @Value("${grpc.client.service-b.port}")
    private int serviceBPort;

    // ObservationRegistry is auto-configured by Spring Boot
    private final ObservationRegistry observationRegistry;

    public GrpcClientConfig(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Bean
    public ManagedChannel managedChannel() {
        // Instantiate the interceptor directly using the ObservationRegistry
        ObservationGrpcClientInterceptor tracingInterceptor = new ObservationGrpcClientInterceptor(observationRegistry);

        return ManagedChannelBuilder.forAddress(serviceBAddress, serviceBPort)
                .intercept(new LoggingClientInterceptor()) // Your existing logging interceptor
                .intercept(tracingInterceptor)          // Add the tracing interceptor from micrometer-core
                .usePlaintext()
                .build();
    }
} 