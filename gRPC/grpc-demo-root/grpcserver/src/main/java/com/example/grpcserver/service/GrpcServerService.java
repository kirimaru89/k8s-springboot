package com.example.grpcserver.service;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.StringRequest;
import com.example.grpc.HelloResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class GrpcServerService extends GreeterGrpc.GreeterImplBase {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerService.class);

    @Override
    public void sayHello(StringRequest request, StreamObserver<HelloResponse> responseObserver) {
        String name = request.getName();
        log.info("Received gRPC request for name: {}", name);
        String message = "Hello " + name;
        HelloResponse response = HelloResponse.newBuilder().setMessage(message).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        log.info("Sent gRPC response: {}", message);
    }
} 