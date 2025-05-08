 # Dự án Demo gRPC

Đây là một dự án demo đơn giản về cách thiết lập gRPC với hai microservice Spring Boot.

- **Service A**: Một service HTTP gọi Service B thông qua gRPC.
- **Service B**: Một service gRPC.

## Điều kiện tiên quyết

- Java 21
- Maven
- Docker
- Docker Compose

## Cấu trúc Dự án

```
grpc-demo-root/
├── proto/hello.proto         # Định nghĩa .proto dùng chung
├── service-a/                # Server HTTP với gRPC client
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
├── service-b/                # Server gRPC
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
├── docker-compose.yml
└── README.md
```

## Chi tiết Luồng hoạt động gRPC

Phần này giải thích cách gRPC được sử dụng để giao tiếp giữa Service A và Service B.

### 1. Định nghĩa Protocol (`proto/hello.proto`)

Hợp đồng giao tiếp giữa gRPC client (trong Service A) và gRPC server (trong Service B) được định nghĩa trong tệp `proto/hello.proto` sử dụng Protocol Buffers.

Các yếu tố chính trong `hello.proto`:
- **`service Greeter`**: Định nghĩa một service tên là `Greeter`.
- **`rpc SayHello (StringRequest) returns (HelloResponse) {}`**: Chỉ định một phương thức gọi thủ tục từ xa (RPC) tên là `SayHello`. Phương thức này nhận một message `StringRequest` làm đầu vào và trả về một message `HelloResponse`.
- **`message StringRequest`**: Định nghĩa cấu trúc của message request, chứa một trường chuỗi duy nhất là `name`.
- **`message HelloResponse`**: Định nghĩa cấu trúc của message response, chứa một trường chuỗi duy nhất là `message`.
- **`option java_package = "com.example.grpc";`**: Chỉ định package Java nơi mã Java được tạo ra sẽ nằm.
- **`option java_multiple_files = true;`**: Hướng dẫn trình tạo mã tạo các tệp Java riêng biệt cho mỗi message và service, thay vì một lớp bên ngoài duy nhất chứa mọi thứ.

### 2. Tạo mã (Build bằng Maven)

Khi bạn build `service-a` và `service-b` bằng Maven (`mvn clean package`), `protobuf-maven-plugin` (được cấu hình trong các tệp `pom.xml` tương ứng) sẽ xử lý `proto/hello.proto`.

- Plugin được cấu hình với `<protoSourceRoot>../proto</protoSourceRoot>`. Điều này cho Maven biết nơi tìm các tệp `.proto` trong một thư mục tên là `proto` nằm ở cấp trên một bậc so với thư mục của service hiện tại (tức là trong `grpc-demo-root/proto/`). Điều này cho phép cả hai service chia sẻ cùng một định nghĩa `hello.proto`.

Plugin này thực hiện hai nhiệm vụ chính:
1.  **Biên dịch `.proto` sang Java**: Nó sử dụng `protoc` (trình biên dịch Protocol Buffer) để tạo các lớp Java từ định nghĩa `.proto`. Các lớp này bao gồm:
    *   `StringRequest.java` và `HelloResponse.java`: Để tạo và thao tác với các message request và response.
    *   `GreeterGrpc.java`: Chứa client stub và server base implementation cho service `Greeter`.
        *   **Client Stub (`GreeterGrpc.GreeterBlockingStub` hoặc `GreeterGrpc.GreeterFutureStub`)**: Được Service A sử dụng để thực hiện các cuộc gọi đến phương thức `SayHello`.
        *   **Lớp Server Cơ sở (`GreeterGrpc.GreeterImplBase`)**: Được Service B kế thừa để triển khai logic thực tế cho phương thức `SayHello`.

Các tệp Java được tạo này được đặt trong các thư mục `target/generated-sources/protobuf/java` và `target/generated-sources/protobuf/grpc-java` của mỗi service và tự động được bao gồm trong classpath biên dịch.

### 3. Service B (Triển khai Server gRPC)

-   **`service-b/src/main/java/com/example/serviceb/service/GrpcServerService.java`**:
    *   Lớp này kế thừa `GreeterGrpc.GreeterImplBase` (lớp cơ sở được tạo ra).
    *   Nó ghi đè phương thức `sayHello` để cung cấp việc triển khai thực tế. Khi được gọi, nó xây dựng một `HelloResponse` và gửi lại cho client.
-   **Khởi động Server gRPC**: Dependency `net.devh:grpc-server-spring-boot-starter` trong `service-b` tự động phát hiện các bean được chú thích bằng `@GrpcService` (như `GrpcServerService`) và khởi động một server gRPC lắng nghe trên cổng đã cấu hình (ví dụ: 9090).

### 4. Service A (Triển khai Client gRPC)

-   **`service-a/src/main/java/com/example/servicea/config/GrpcClientConfig.java`**:
    *   Lớp cấu hình này tạo ra một `ManagedChannel`. Một `ManagedChannel` đại diện cho một kết nối đến một server gRPC.
    *   Nó được cấu hình để kết nối đến Service B (địa chỉ `service-b`, cổng `9090` - được ghi đè bởi Docker Compose để giao tiếp giữa các container).
    *   Nó cũng đăng ký các interceptor như `LoggingClientInterceptor` (để ghi log tùy chỉnh) và `ObservationGrpcClientInterceptor` (để theo dõi phân tán - distributed tracing).
-   **`service-a/src/main/java/com/example/servicea/client/GrpcClient.java`**:
    *   Thành phần client này nhận `ManagedChannel` làm dependency.
    *   Nó tạo ra một gRPC client stub bằng cách sử dụng `GreeterGrpc.newBlockingStub(channel)`.
    *   Phương thức `sayHello(String name)` trong client này sử dụng stub để:
        1.  Tạo một `StringRequest`.
        2.  Gọi phương thức RPC `sayHello` trên stub, gửi request.
        3.  Nhận `HelloResponse` từ Service B.
-   **Tích hợp**: `HelloController` -> `GreetingService` -> `GrpcClient` là luồng trong Service A để kích hoạt cuộc gọi gRPC.

### Tóm tắt Tương tác

1.  Người dùng gửi một HTTP GET request đến `/hello/{name}` trên Service A.
2.  `HelloController` của Service A gọi `GreetingService`.
3.  `GreetingService` gọi `GrpcClient`.
4.  `GrpcClient` sử dụng `GreeterGrpc.GreeterBlockingStub` được tạo ra (đã cấu hình với một `ManagedChannel` trỏ đến Service B) để gọi phương thức RPC `sayHello`.
    *   Client stub tuần tự hóa `StringRequest` thành định dạng nhị phân Protocol Buffer.
    *   Ngữ cảnh theo dõi (trace context), nếu được cấu hình (ví dụ với Micrometer), sẽ được tiêm vào metadata (headers) của gRPC.
5.  Request di chuyển qua mạng đến server gRPC của Service B.
6.  `GrpcServerService` của Service B (triển khai `GreeterImplBase`) nhận request.
    *   Ngữ cảnh theo dõi được trích xuất từ metadata.
    *   Server giải tuần tự hóa `StringRequest`.
7.  `GrpcServerService` xử lý request và tạo một `HelloResponse`.
8.  Server tuần tự hóa `HelloResponse` và gửi lại.
9.  `GrpcClient` của Service A nhận `HelloResponse`, giải tuần tự hóa nó và trả về nội dung message.
10. Service A gửi message trở lại dưới dạng HTTP response.

Thiết lập này cho phép giao tiếp hiệu quả, được định kiểu mạnh mẽ giữa các microservice, tận dụng Protocol Buffers để tuần tự hóa và gRPC cho cơ chế RPC.

## Cách Build và Chạy

1.  **Build các service bằng Maven:**

    Điều hướng đến thư mục `grpc-demo-root`.

    Build Service A:
    ```bash
    cd service-a
    mvn clean package
    cd ..
    ```

    Build Service B:
    ```bash
    cd service-b
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

Khi các service đang chạy, bạn có thể kiểm tra endpoint của Service A:

Mở trình duyệt của bạn hoặc sử dụng một công cụ như `curl`:

```bash
curl http://localhost:8080/hello/YourName
```

Kết quả trả về sẽ là:

```
Hello YourName
```

(Nếu Service B xử lý thành công cuộc gọi gRPC, message thực tế có thể là "Hello YourName" hoặc tương tự, tùy thuộc vào logic của Service B là nối thêm "Hello ")

## Kiểm tra Sức khỏe (Health Checks)

- Service A Health: `http://localhost:8080/actuator/health`
- Service B Health (qua gRPC, endpoint HTTP thực tế phụ thuộc vào cấu hình Actuator nếu được expose qua HTTP cho service gRPC):
  Việc kiểm tra sức khỏe gRPC thường được xử lý bởi `grpc-health-probe` hoặc các công cụ tương tự. Spring Boot Actuator cho các service gRPC có thể yêu cầu cấu hình bổ sung để expose các endpoint sức khỏe HTTP nếu không sử dụng trực tiếp một service kiểm tra sức khỏe gRPC.
  Đối với dự án này, sức khỏe của Service B có thể được suy ra từ log của nó và các phản hồi thành công cho Service A.

## Dừng các Service

Để dừng các service đang chạy qua Docker Compose:

```bash
docker compose down
```
