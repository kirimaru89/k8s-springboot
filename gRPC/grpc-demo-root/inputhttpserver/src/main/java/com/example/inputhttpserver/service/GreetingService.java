package com.example.inputhttpserver.service;

import com.example.inputhttpserver.client.GrpcClient;
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