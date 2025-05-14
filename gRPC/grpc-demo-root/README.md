# Dự án Demo gRPC

Đây là một dự án demo đơn giản về cách thiết lập gRPC với hai microservice Spring Boot.

- **InputHttpServer**: Một service HTTP gọi GrpcServer thông qua gRPC.
- **GrpcServer**: Một service gRPC.

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

Các thành phần trong file `hello.proto`:
- **`service Greeter`**: Định nghĩa một service có tên `Greeter`.
- **`rpc SayHello (StringRequest) returns (HelloResponse) {}`**: Chỉ định một phương thức gọi thủ tục từ xa (RPC) có tên `SayHello`. Phương thức này nhận một message `StringRequest` làm đầu vào và trả về một message `HelloResponse`.
- **`message StringRequest`**: Định nghĩa cấu trúc của message request, chứa một trường string `name`.
- **`message HelloResponse`**: Định nghĩa cấu trúc của message response, chứa một trường string `message`.
- **`option java_package = "com.example.grpc";`**: Chỉ định package Java nơi code Java được tạo ra sẽ được đặt.
- **`option java_multiple_files = true;`**: Hướng dẫn trình tạo tạo các file Java riêng biệt cho mỗi message và service, thay vì một class bên ngoài duy nhất chứa tất cả.

### 2. Code Generation (Maven Build)

Khi bạn build `inputhttpserver` và `grpcserver` bằng Maven (`mvn clean package`), `protobuf-maven-plugin` (được cấu hình trong các tệp `pom.xml` tương ứng) sẽ xử lý `proto/hello.proto`.

- Plugin được cấu hình với `<protoSourceRoot>../proto</protoSourceRoot>`. Điều này cho Maven biết nơi tìm các tệp `.proto` trong một thư mục tên là `proto` nằm ở cấp trên một bậc so với thư mục của service hiện tại (tức là trong `grpc-demo-root/proto/`). Điều này cho phép cả hai service chia sẻ cùng một định nghĩa `hello.proto`.

Plugin này thực hiện hai nhiệm vụ chính:
1.  **Biên dịch `.proto` sang Java**: Nó sử dụng `protoc` (trình biên dịch Protocol Buffer) để tạo các class Java từ định nghĩa `.proto`. Các class này bao gồm:
    *   `StringRequest.java` và `HelloResponse.java`: Để tạo và thao tác với các message request và response.
    *   `GreeterGrpc.java`: Chứa client stub và triển khai cơ sở của server cho service `Greeter`.
        *   **Client Stub (`GreeterGrpc.GreeterBlockingStub` hoặc `GreeterGrpc.GreeterFutureStub`)**: Được InputHttpServer sử dụng để gọi phương thức `SayHello`.
        *   **Server Base Class (`GreeterGrpc.GreeterImplBase`)**: Được GrpcServer mở rộng để triển khai logic thực tế cho phương thức `SayHello`.

Các file Java được tạo ra này được đặt trong thư mục `target/generated-sources/protobuf/java` và `target/generated-sources/protobuf/grpc-java` của mỗi service và được tự động đưa vào classpath khi biên dịch.

### 3. GrpcServer (Triển khai Server gRPC)

-   **`grpcserver/src/main/java/com/example/grpcserver/service/GrpcServerService.java`**:
    *   Class này mở rộng `GreeterGrpc.GreeterImplBase` (class cơ sở được tạo ra).
    *   Nó ghi đè phương thức `sayHello` để cung cấp triển khai thực tế. Khi được gọi, nó tạo một `HelloResponse` và gửi lại cho client.
-   **Khởi động Server gRPC**: Dependency `net.devh:grpc-server-spring-boot-starter` trong `grpcserver` tự động phát hiện các bean được đánh dấu với `@GrpcService` (như `GrpcServerService`) và khởi động một gRPC server lắng nghe trên cổng được cấu hình (ví dụ: 9090).

### 4. InputHttpServer (Triển khai Client gRPC)

-   **`inputhttpserver/src/main/java/com/example/inputhttpserver/config/GrpcClientConfig.java`**:
    *   Class cấu hình này tạo một `ManagedChannel`. Một `ManagedChannel` đại diện cho một kết nối đến gRPC server.
    *   Nó được cấu hình để kết nối đến GrpcServer (địa chỉ `grpcserver`, cổng `9090` - được ghi đè bởi Docker Compose cho giao tiếp giữa các container).
    *   Nó cũng đăng ký các interceptor như `LoggingClientInterceptor` (cho logging tùy chỉnh) và `ObservationGrpcClientInterceptor` (cho distributed tracing).
-   **`inputhttpserver/src/main/java/com/example/inputhttpserver/client/GrpcClient.java`**:
    *   Component client này nhận `ManagedChannel` như một dependency.
    *   Nó tạo một gRPC client stub sử dụng `GreeterGrpc.newBlockingStub(channel)`.
    *   Phương thức `sayHello(String name)` trong client này sử dụng stub để:
        1.  Tạo một `StringRequest`.
        2.  Gọi phương thức RPC `sayHello` trên stub, gửi request.
        3.  Nhận `HelloResponse` từ GrpcServer.
-   **Tích hợp**: `HelloController` -> `GreetingService` -> `GrpcClient` là luồng trong InputHttpServer để kích hoạt cuộc gọi gRPC.

### Tóm tắt Tương tác

1.  Người dùng gửi một HTTP GET request đến `/hello/{name}` trên InputHttpServer.
2.  `HelloController` của InputHttpServer gọi `GreetingService`.
3.  `GreetingService` gọi `GrpcClient`.
4.  `GrpcClient` sử dụng `GreeterGrpc.GreeterBlockingStub` được tạo ra (được cấu hình với một `ManagedChannel` trỏ đến GrpcServer) để gọi phương thức RPC `sayHello`.
    *   Client stub serialize `StringRequest` thành định dạng nhị phân Protocol Buffer.
    *   Trace context (nếu được cấu hình, như với Micrometer) được chèn vào metadata gRPC (headers).
5.  Request đi qua mạng đến gRPC server của GrpcServer.
6.  `GrpcServerService` của GrpcServer (triển khai của `GreeterImplBase`) nhận request.
    *   Trace context được trích xuất từ metadata.
    *   Server deserialize `StringRequest`.
7.  `GrpcServerService` xử lý request và tạo một `HelloResponse`.
8.  Server serialize `HelloResponse` và gửi lại.
9.  `GrpcClient` của InputHttpServer nhận `HelloResponse`, deserialize nó, và trả về nội dung message.
10. InputHttpServer gửi message trở lại như là HTTP response.

Cấu hình này cho phép giao tiếp hiệu quả, có kiểu dữ liệu mạnh giữa các microservice, tận dụng Protocol Buffers cho serialization và gRPC cho cơ chế RPC.

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

## Dừng các Service

Để dừng các service đang chạy qua Docker Compose:

```bash
docker compose down
```
