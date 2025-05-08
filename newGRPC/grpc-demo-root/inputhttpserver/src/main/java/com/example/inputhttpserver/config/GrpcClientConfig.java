package com.example.inputhttpserver.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.core.instrument.binder.grpc.ObservationGrpcClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.grpcserver.address}") // Updated to reflect new service name in properties
    private String grpcServerAddress;

    @Value("${grpc.client.grpcserver.port}") // Updated to reflect new service name in properties
    private int grpcServerPort;

    private final ObservationRegistry observationRegistry;
    private final LoggingClientInterceptor loggingClientInterceptor; // Assuming LoggingClientInterceptor is in the same package or imported

    public GrpcClientConfig(ObservationRegistry observationRegistry, LoggingClientInterceptor loggingClientInterceptor) {
        this.observationRegistry = observationRegistry;
        this.loggingClientInterceptor = loggingClientInterceptor;
    }

    @Bean
    public ManagedChannel managedChannel() {
        ObservationGrpcClientInterceptor tracingInterceptor = new ObservationGrpcClientInterceptor(observationRegistry);

        return ManagedChannelBuilder.forAddress(grpcServerAddress, grpcServerPort)
                .intercept(loggingClientInterceptor)      // Use injected logging interceptor
                .intercept(tracingInterceptor)
                .usePlaintext()
                .build();
    }
} 