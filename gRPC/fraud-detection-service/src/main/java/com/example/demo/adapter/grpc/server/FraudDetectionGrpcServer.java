package com.example.demo.adapter.grpc.server;

import com.example.demo.services.FraudDetectionService;
import com.vtbank.digitaldocuments.library.v1.FraudDetectionServiceGrpc;
import digitaldocuments.library.v1.FraudDetectionMessages;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class FraudDetectionGrpcServer extends FraudDetectionServiceGrpc.FraudDetectionServiceImplBase {
    FraudDetectionService fraudDetectionService;

    public FraudDetectionGrpcServer (FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @Override
    public void createFraudDetection(FraudDetectionMessages.CreateFraudDetectionRequest request, StreamObserver<FraudDetectionMessages.FraudDetection> responseObserver) {
        FraudDetectionMessages.FraudDetection fraudDetection = fraudDetectionService.createFraudDetection(request);
        responseObserver.onNext(fraudDetection);
        responseObserver.onCompleted();
    }

}