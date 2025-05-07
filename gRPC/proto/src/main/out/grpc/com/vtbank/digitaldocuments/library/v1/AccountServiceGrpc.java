package com.vtbank.digitaldocuments.library.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.59.0)",
    comments = "Source: digitaldocuments/library/v1/account/account_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class AccountServiceGrpc {

  private AccountServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "digitaldocuments.library.v1.AccountService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.GetAccountRequest,
      digitaldocuments.library.v1.AccountMessages.Account> getGetAccountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccount",
      requestType = digitaldocuments.library.v1.AccountMessages.GetAccountRequest.class,
      responseType = digitaldocuments.library.v1.AccountMessages.Account.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.GetAccountRequest,
      digitaldocuments.library.v1.AccountMessages.Account> getGetAccountMethod() {
    io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.GetAccountRequest, digitaldocuments.library.v1.AccountMessages.Account> getGetAccountMethod;
    if ((getGetAccountMethod = AccountServiceGrpc.getGetAccountMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getGetAccountMethod = AccountServiceGrpc.getGetAccountMethod) == null) {
          AccountServiceGrpc.getGetAccountMethod = getGetAccountMethod =
              io.grpc.MethodDescriptor.<digitaldocuments.library.v1.AccountMessages.GetAccountRequest, digitaldocuments.library.v1.AccountMessages.Account>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  digitaldocuments.library.v1.AccountMessages.GetAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  digitaldocuments.library.v1.AccountMessages.Account.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("GetAccount"))
              .build();
        }
      }
    }
    return getGetAccountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.ListAccountsRequest,
      digitaldocuments.library.v1.AccountMessages.ListAccountsResponse> getListAccountsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListAccounts",
      requestType = digitaldocuments.library.v1.AccountMessages.ListAccountsRequest.class,
      responseType = digitaldocuments.library.v1.AccountMessages.ListAccountsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.ListAccountsRequest,
      digitaldocuments.library.v1.AccountMessages.ListAccountsResponse> getListAccountsMethod() {
    io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.ListAccountsRequest, digitaldocuments.library.v1.AccountMessages.ListAccountsResponse> getListAccountsMethod;
    if ((getListAccountsMethod = AccountServiceGrpc.getListAccountsMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getListAccountsMethod = AccountServiceGrpc.getListAccountsMethod) == null) {
          AccountServiceGrpc.getListAccountsMethod = getListAccountsMethod =
              io.grpc.MethodDescriptor.<digitaldocuments.library.v1.AccountMessages.ListAccountsRequest, digitaldocuments.library.v1.AccountMessages.ListAccountsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListAccounts"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  digitaldocuments.library.v1.AccountMessages.ListAccountsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  digitaldocuments.library.v1.AccountMessages.ListAccountsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("ListAccounts"))
              .build();
        }
      }
    }
    return getListAccountsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.CreateAccountRequest,
      digitaldocuments.library.v1.AccountMessages.Account> getCreateAccountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateAccount",
      requestType = digitaldocuments.library.v1.AccountMessages.CreateAccountRequest.class,
      responseType = digitaldocuments.library.v1.AccountMessages.Account.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.CreateAccountRequest,
      digitaldocuments.library.v1.AccountMessages.Account> getCreateAccountMethod() {
    io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.CreateAccountRequest, digitaldocuments.library.v1.AccountMessages.Account> getCreateAccountMethod;
    if ((getCreateAccountMethod = AccountServiceGrpc.getCreateAccountMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getCreateAccountMethod = AccountServiceGrpc.getCreateAccountMethod) == null) {
          AccountServiceGrpc.getCreateAccountMethod = getCreateAccountMethod =
              io.grpc.MethodDescriptor.<digitaldocuments.library.v1.AccountMessages.CreateAccountRequest, digitaldocuments.library.v1.AccountMessages.Account>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateAccount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  digitaldocuments.library.v1.AccountMessages.CreateAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  digitaldocuments.library.v1.AccountMessages.Account.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("CreateAccount"))
              .build();
        }
      }
    }
    return getCreateAccountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest,
      digitaldocuments.library.v1.AccountMessages.Account> getUpdateAccountBalanceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateAccountBalance",
      requestType = digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest.class,
      responseType = digitaldocuments.library.v1.AccountMessages.Account.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest,
      digitaldocuments.library.v1.AccountMessages.Account> getUpdateAccountBalanceMethod() {
    io.grpc.MethodDescriptor<digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest, digitaldocuments.library.v1.AccountMessages.Account> getUpdateAccountBalanceMethod;
    if ((getUpdateAccountBalanceMethod = AccountServiceGrpc.getUpdateAccountBalanceMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getUpdateAccountBalanceMethod = AccountServiceGrpc.getUpdateAccountBalanceMethod) == null) {
          AccountServiceGrpc.getUpdateAccountBalanceMethod = getUpdateAccountBalanceMethod =
              io.grpc.MethodDescriptor.<digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest, digitaldocuments.library.v1.AccountMessages.Account>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateAccountBalance"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  digitaldocuments.library.v1.AccountMessages.Account.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("UpdateAccountBalance"))
              .build();
        }
      }
    }
    return getUpdateAccountBalanceMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AccountServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AccountServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AccountServiceStub>() {
        @java.lang.Override
        public AccountServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AccountServiceStub(channel, callOptions);
        }
      };
    return AccountServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AccountServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AccountServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AccountServiceBlockingStub>() {
        @java.lang.Override
        public AccountServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AccountServiceBlockingStub(channel, callOptions);
        }
      };
    return AccountServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static AccountServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AccountServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AccountServiceFutureStub>() {
        @java.lang.Override
        public AccountServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AccountServiceFutureStub(channel, callOptions);
        }
      };
    return AccountServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getAccount(digitaldocuments.library.v1.AccountMessages.GetAccountRequest request,
        io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.Account> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetAccountMethod(), responseObserver);
    }

    /**
     */
    default void listAccounts(digitaldocuments.library.v1.AccountMessages.ListAccountsRequest request,
        io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.ListAccountsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListAccountsMethod(), responseObserver);
    }

    /**
     */
    default void createAccount(digitaldocuments.library.v1.AccountMessages.CreateAccountRequest request,
        io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.Account> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateAccountMethod(), responseObserver);
    }

    /**
     */
    default void updateAccountBalance(digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest request,
        io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.Account> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateAccountBalanceMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service AccountService.
   */
  public static abstract class AccountServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return AccountServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service AccountService.
   */
  public static final class AccountServiceStub
      extends io.grpc.stub.AbstractAsyncStub<AccountServiceStub> {
    private AccountServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AccountServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AccountServiceStub(channel, callOptions);
    }

    /**
     */
    public void getAccount(digitaldocuments.library.v1.AccountMessages.GetAccountRequest request,
        io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.Account> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetAccountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listAccounts(digitaldocuments.library.v1.AccountMessages.ListAccountsRequest request,
        io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.ListAccountsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListAccountsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createAccount(digitaldocuments.library.v1.AccountMessages.CreateAccountRequest request,
        io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.Account> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateAccountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateAccountBalance(digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest request,
        io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.Account> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateAccountBalanceMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service AccountService.
   */
  public static final class AccountServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<AccountServiceBlockingStub> {
    private AccountServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AccountServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AccountServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public digitaldocuments.library.v1.AccountMessages.Account getAccount(digitaldocuments.library.v1.AccountMessages.GetAccountRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetAccountMethod(), getCallOptions(), request);
    }

    /**
     */
    public digitaldocuments.library.v1.AccountMessages.ListAccountsResponse listAccounts(digitaldocuments.library.v1.AccountMessages.ListAccountsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListAccountsMethod(), getCallOptions(), request);
    }

    /**
     */
    public digitaldocuments.library.v1.AccountMessages.Account createAccount(digitaldocuments.library.v1.AccountMessages.CreateAccountRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateAccountMethod(), getCallOptions(), request);
    }

    /**
     */
    public digitaldocuments.library.v1.AccountMessages.Account updateAccountBalance(digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateAccountBalanceMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service AccountService.
   */
  public static final class AccountServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<AccountServiceFutureStub> {
    private AccountServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AccountServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AccountServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<digitaldocuments.library.v1.AccountMessages.Account> getAccount(
        digitaldocuments.library.v1.AccountMessages.GetAccountRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetAccountMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<digitaldocuments.library.v1.AccountMessages.ListAccountsResponse> listAccounts(
        digitaldocuments.library.v1.AccountMessages.ListAccountsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListAccountsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<digitaldocuments.library.v1.AccountMessages.Account> createAccount(
        digitaldocuments.library.v1.AccountMessages.CreateAccountRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateAccountMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<digitaldocuments.library.v1.AccountMessages.Account> updateAccountBalance(
        digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateAccountBalanceMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_ACCOUNT = 0;
  private static final int METHODID_LIST_ACCOUNTS = 1;
  private static final int METHODID_CREATE_ACCOUNT = 2;
  private static final int METHODID_UPDATE_ACCOUNT_BALANCE = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_ACCOUNT:
          serviceImpl.getAccount((digitaldocuments.library.v1.AccountMessages.GetAccountRequest) request,
              (io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.Account>) responseObserver);
          break;
        case METHODID_LIST_ACCOUNTS:
          serviceImpl.listAccounts((digitaldocuments.library.v1.AccountMessages.ListAccountsRequest) request,
              (io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.ListAccountsResponse>) responseObserver);
          break;
        case METHODID_CREATE_ACCOUNT:
          serviceImpl.createAccount((digitaldocuments.library.v1.AccountMessages.CreateAccountRequest) request,
              (io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.Account>) responseObserver);
          break;
        case METHODID_UPDATE_ACCOUNT_BALANCE:
          serviceImpl.updateAccountBalance((digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest) request,
              (io.grpc.stub.StreamObserver<digitaldocuments.library.v1.AccountMessages.Account>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetAccountMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              digitaldocuments.library.v1.AccountMessages.GetAccountRequest,
              digitaldocuments.library.v1.AccountMessages.Account>(
                service, METHODID_GET_ACCOUNT)))
        .addMethod(
          getListAccountsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              digitaldocuments.library.v1.AccountMessages.ListAccountsRequest,
              digitaldocuments.library.v1.AccountMessages.ListAccountsResponse>(
                service, METHODID_LIST_ACCOUNTS)))
        .addMethod(
          getCreateAccountMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              digitaldocuments.library.v1.AccountMessages.CreateAccountRequest,
              digitaldocuments.library.v1.AccountMessages.Account>(
                service, METHODID_CREATE_ACCOUNT)))
        .addMethod(
          getUpdateAccountBalanceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              digitaldocuments.library.v1.AccountMessages.UpdateAccountBalanceRequest,
              digitaldocuments.library.v1.AccountMessages.Account>(
                service, METHODID_UPDATE_ACCOUNT_BALANCE)))
        .build();
  }

  private static abstract class AccountServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    AccountServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.vtbank.digitaldocuments.library.v1.AccountProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("AccountService");
    }
  }

  private static final class AccountServiceFileDescriptorSupplier
      extends AccountServiceBaseDescriptorSupplier {
    AccountServiceFileDescriptorSupplier() {}
  }

  private static final class AccountServiceMethodDescriptorSupplier
      extends AccountServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    AccountServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (AccountServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AccountServiceFileDescriptorSupplier())
              .addMethod(getGetAccountMethod())
              .addMethod(getListAccountsMethod())
              .addMethod(getCreateAccountMethod())
              .addMethod(getUpdateAccountBalanceMethod())
              .build();
        }
      }
    }
    return result;
  }
}
