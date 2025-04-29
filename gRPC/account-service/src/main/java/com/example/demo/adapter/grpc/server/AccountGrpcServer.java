package com.example.demo.adapter.grpc.server;

import com.vtbank.digitaldocuments.library.v1.AccountServiceGrpc;
import com.example.demo.services.AccountService;
import digitaldocuments.library.v1.AccountMessages;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GrpcService
public class AccountGrpcServer extends AccountServiceGrpc.AccountServiceImplBase {

    private final AccountService accountService;

    @Autowired
    public AccountGrpcServer(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void getAccount(AccountMessages.GetAccountRequest request, StreamObserver<AccountMessages.Account> responseObserver) {
        AccountMessages.Account account = accountService.getAccountById(request);
        responseObserver.onNext(account);
        responseObserver.onCompleted();
    }

    @Override
    public void createAccount(AccountMessages.CreateAccountRequest request, StreamObserver<AccountMessages.Account> responseObserver) {
        AccountMessages.Account account = accountService.createAccount(request);
        responseObserver.onNext(account);
        responseObserver.onCompleted();
    }

    @Override
    public void listAccounts(AccountMessages.ListAccountsRequest request, StreamObserver<AccountMessages.ListAccountsResponse> responseObserver) {
        AccountMessages.ListAccountsResponse accounts = accountService.getAccountsByUserId(request);
        responseObserver.onNext(accounts);
        responseObserver.onCompleted();
    }

    @Override
    public void updateAccountBalance(AccountMessages.UpdateAccountBalanceRequest request, StreamObserver<AccountMessages.Account> responseObserver) {
        AccountMessages.Account account = accountService.updateAccountBalance(request);
        responseObserver.onNext(account);
        responseObserver.onCompleted();
    }

} 