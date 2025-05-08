package com.example.servicea.service;

import com.example.servicea.client.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    private final GrpcClient grpcClient;

    public GreetingService(GrpcClient grpcClient) {
        this.grpcClient = grpcClient;
    }

    public String greet(String name) {
        return grpcClient.sayHello(name);
    }
} 