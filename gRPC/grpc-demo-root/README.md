# Dự án Demo gRPC

Đây là một dự án demo đơn giản về cách thiết lập gRPC với hai microservice Spring Boot.

- **InputHttpServer**: Một service HTTP gọi GrpcServer thông qua gRPC. (Trước đây là Service A)
- **GrpcServer**: Một service gRPC. (Trước đây là Service B)

## Điều kiện tiên quyết

- Java 21
- Maven
- Docker
- Docker Compose

## Cấu trúc Dự án

```
grpc-demo-root/
├── proto/hello.proto         # Định nghĩa .proto dùng chung
├── inputhttpserver/          # Server HTTP với gRPC client (Trước đây là service-a)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
├── grpcserver/               # Server gRPC (Trước đây là service-b)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
├── docker-compose.yml
└── README.md
```

## Chi tiết Luồng hoạt động gRPC

Phần này giải thích cách gRPC được sử dụng để giao tiếp giữa InputHttpServer và GrpcServer.

### 1. Protocol Definition (`proto/hello.proto`)

Hợp đồng giao tiếp giữa gRPC client (trong InputHttpServer) và gRPC server (trong GrpcServer) được định nghĩa trong tệp `proto/hello.proto` sử dụng Protocol Buffers.

Key elements in `hello.proto`:
- **`service Greeter`**: Defines a service named `Greeter`.
- **`rpc SayHello (StringRequest) returns (HelloResponse) {}`**: Specifies a remote procedure call (RPC) method named `SayHello`. This method takes a `StringRequest` message as input and returns a `HelloResponse` message.
- **`message StringRequest`**: Defines the structure of the request message, containing a single string field `name`.
- **`message HelloResponse`**: Defines the structure of the response message, containing a single string field `message`.
- **`option java_package = "com.example.grpc";`**: Specifies the Java package where the generated Java code will reside.
- **`option java_multiple_files = true;`**: Instructs the generator to create separate Java files for each message and service, rather than a single outer class containing everything.

### 2. Code Generation (Maven Build)

Khi bạn build `inputhttpserver` và `grpcserver` bằng Maven (`mvn clean package`), `protobuf-maven-plugin` (được cấu hình trong các tệp `pom.xml` tương ứng) sẽ xử lý `proto/hello.proto`.

- Plugin được cấu hình với `<protoSourceRoot>../proto</protoSourceRoot>`. Điều này cho Maven biết nơi tìm các tệp `.proto` trong một thư mục tên là `proto` nằm ở cấp trên một bậc so với thư mục của service hiện tại (tức là trong `grpc-demo-root/proto/`). Điều này cho phép cả hai service chia sẻ cùng một định nghĩa `hello.proto`.

Plugin này thực hiện hai nhiệm vụ chính:
1.  **Biên dịch `.proto` sang Java**: It uses `protoc` (the Protocol Buffer compiler) to generate Java classes from the `.proto` definition. These classes include:
    *   `StringRequest.java` and `HelloResponse.java`: For creating and manipulating request and response messages.
    *   `GreeterGrpc.java`: Contains the client stub and server base implementation for the `Greeter` service.
        *   **Client Stub (`GreeterGrpc.GreeterBlockingStub` or `GreeterGrpc.GreeterFutureStub`)**: Used by InputHttpServer to make calls to the `SayHello` method.
        *   **Server Base Class (`GreeterGrpc.GreeterImplBase`)**: Extended by GrpcServer to implement the actual logic for the `SayHello` method.

These generated Java files are placed in the `target/generated-sources/protobuf/java` and `target/generated-sources/protobuf/grpc-java` directories of each service and are automatically included in the compilation classpath.

### 3. GrpcServer (Triển khai Server gRPC)

-   **`grpcserver/src/main/java/com/example/grpcserver/service/GrpcServerService.java`**:
    *   This class extends `GreeterGrpc.GreeterImplBase` (the generated base class).
    *   It overrides the `sayHello` method to provide the actual implementation. When called, it constructs a `HelloResponse` and sends it back to the client.
-   **Khởi động Server gRPC**: The `net.devh:grpc-server-spring-boot-starter` dependency in `grpcserver` automatically discovers beans annotated with `@GrpcService` (like `GrpcServerService`) and starts a gRPC server listening on the configured port (e.g., 9090).

### 4. InputHttpServer (Triển khai Client gRPC)

-   **`inputhttpserver/src/main/java/com/example/inputhttpserver/config/GrpcClientConfig.java`**:
    *   This configuration class creates a `ManagedChannel`. A `ManagedChannel` represents a connection to a gRPC server.
    *   It's configured to connect to GrpcServer (address `grpcserver`, port `9090` - overridden by Docker Compose for inter-container communication).
    *   It also registers interceptors like `LoggingClientInterceptor` (for custom logging) and `ObservationGrpcClientInterceptor` (for distributed tracing).
-   **`inputhttpserver/src/main/java/com/example/inputhttpserver/client/GrpcClient.java`**:
    *   This client component receives the `ManagedChannel` as a dependency.
    *   It creates a gRPC client stub using `GreeterGrpc.newBlockingStub(channel)`.
    *   The `sayHello(String name)` method in this client uses the stub to:
        1.  Create a `StringRequest`.
        2.  Call the `sayHello` RPC method on the stub, sending the request.
        3.  Receive the `HelloResponse` from GrpcServer.
-   **Tích hợp**: `HelloController` -> `GreetingService` -> `GrpcClient` is the flow within InputHttpServer to trigger the gRPC call.

### Tóm tắt Tương tác

1.  Người dùng gửi một HTTP GET request đến `/hello/{name}` trên InputHttpServer.
2.  `HelloController` của InputHttpServer gọi `GreetingService`.
3.  `GreetingService` gọi `GrpcClient`.
4.  `GrpcClient` uses the generated `GreeterGrpc.GreeterBlockingStub` (configured with a `ManagedChannel` pointing to GrpcServer) to invoke the `sayHello` RPC method.
    *   The client stub serializes the `StringRequest` into the Protocol Buffer binary format.
    *   Trace context (if configured, like with Micrometer) is injected into the gRPC metadata (headers).
5.  The request travels over the network to GrpcServer's gRPC server.
6.  `GrpcServerService` của GrpcServer (the `GreeterImplBase` implementation) receives the request.
    *   Trace context is extracted from metadata.
    *   The server deserializes the `StringRequest`.
7.  `GrpcServerService` processes the request and creates a `HelloResponse`.
8.  The server serializes the `HelloResponse` and sends it back.
9.  `GrpcClient` của InputHttpServer receives the `HelloResponse`, deserializes it, and returns the message content.
10. InputHttpServer sends the message back as the HTTP response.

This setup allows for efficient, strongly-typed communication between the microservices, leveraging Protocol Buffers for serialization and gRPC for the RPC mechanism.

## Cách Build và Chạy

1.  **Build các service bằng Maven:**

    Điều hướng đến thư mục `grpc-demo-root`.

    Build InputHttpServer:
    ```bash
    cd inputhttpserver
    mvn clean package
    cd ..
    ```

    Build GrpcServer:
    ```bash
    cd grpcserver
    mvn clean package
    cd ..
    ```

2.  **Chạy bằng Docker Compose:**

    Đảm bảo bạn đang ở trong thư mục `grpc-demo-root` nơi có tệp `docker-compose.yml`.

    ```bash
    docker compose up --build
    ```

    Lệnh này sẽ build các Docker image (nếu chưa được build hoặc nếu có thay đổi được phát hiện) và khởi động cả hai service.

## Kiểm tra Endpoint

Khi các service đang chạy, bạn có thể kiểm tra endpoint của InputHttpServer:

Mở trình duyệt của bạn hoặc sử dụng một công cụ như `curl`:

```bash
curl http://localhost:8080/hello/YourName
```

Kết quả trả về sẽ là:

```
Hello YourName
```

(If GrpcServer successfully processed the gRPC call, the actual message might be like "Hello YourName" or similar, depending on GrpcServer's logic which is to prepend "Hello ")

## Kiểm tra Sức khỏe (Health Checks)

- InputHttpServer Health: `http://localhost:8080/actuator/health`
- GrpcServer Health (via gRPC, actual HTTP endpoint depends on Actuator config if exposed over HTTP for gRPC service):
  The gRPC health check is typically handled by the `grpc-health-probe` or similar tools. Spring Boot Actuator for gRPC services might require additional configuration to expose HTTP health endpoints if not using a gRPC health check service directly.
  For this project, GrpcServer's health can be inferred from its logs and successful responses to InputHttpServer.

## Dừng các Service

Để dừng các service đang chạy qua Docker Compose:

```bash
docker compose down
```
