package com.example.servicea.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.service-b.address}")
    private String serviceBAddress;

    @Value("${grpc.client.service-b.port}")
    private int serviceBPort;

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress(serviceBAddress, serviceBPort)
                .usePlaintext()
                .build();
    }
} 