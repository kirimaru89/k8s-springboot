package com.example.demo.adapter.grpc.server;

import com.vtbank.digitaldocuments.library.v1.TransactionServiceGrpc;
import digitaldocuments.library.v1.TransactionMessages.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import com.example.demo.services.TransactionService;


@GrpcService
@RequiredArgsConstructor
@Slf4j
public class TransactionGrpcServer extends TransactionServiceGrpc.TransactionServiceImplBase {

    private final TransactionService transactionService;

    @Override
    public void createTransaction(CreateTransactionRequest request, StreamObserver<Transaction> responseObserver) {
        Transaction transactionResponse = transactionService.createTransaction(request);
        responseObserver.onNext(transactionResponse);
        responseObserver.onCompleted();
    }

} 