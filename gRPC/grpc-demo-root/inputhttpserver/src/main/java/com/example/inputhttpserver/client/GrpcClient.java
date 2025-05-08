package com.example.inputhttpserver.client;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.StringRequest;
import com.example.grpc.HelloResponse;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GrpcClient {

    private static final Logger log = LoggerFactory.getLogger(GrpcClient.class);
    private final GreeterGrpc.GreeterBlockingStub greeterStub;

    public GrpcClient(ManagedChannel channel) {
        this.greeterStub = GreeterGrpc.newBlockingStub(channel);
    }

    public String sayHello(String name) {
        StringRequest request = StringRequest.newBuilder().setName(name).build();
        log.info("Calling gRPC service with name: {}", name);
        HelloResponse response = greeterStub.sayHello(request);
        log.info("Received gRPC response: {}", response.getMessage());
        return response.getMessage();
    }
} 