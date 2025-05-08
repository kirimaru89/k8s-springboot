package com.example.inputhttpserver.service;

import org.springframework.stereotype.Service;

import com.example.inputhttpserver.client.GrpcClient;

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