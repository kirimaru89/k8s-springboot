package com.example.demo.adapter.grpc.client;

import com.vtbank.digitaldocuments.library.v1.FraudDetectionServiceGrpc;
import digitaldocuments.library.v1.FraudDetectionMessages;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class FraudDetectionGrpcClient {

    @GrpcClient("fraud-detection-service")
    private FraudDetectionServiceGrpc.FraudDetectionServiceBlockingStub fraudDetectionServiceBlockingStub;

    public FraudDetectionMessages.FraudDetection createFraudDetection(FraudDetectionMessages.CreateFraudDetectionRequest request) {
        return fraudDetectionServiceBlockingStub.createFraudDetection(request);
    }

}

