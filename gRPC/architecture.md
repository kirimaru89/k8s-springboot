# Hướng dẫn sử dụng hệ thống Microservices

## Mục lục
1. [Tổng quan về hệ thống](#tổng-quan-về-hệ-thống)
2. [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
3. [Công nghệ sử dụng](#công-nghệ-sử-dụng)
4. [Cài đặt và triển khai](#cài-đặt-và-triển-khai)
5. [Cấu trúc các service](#cấu-trúc-các-service)
6. [Hướng dẫn sử dụng](#hướng-dẫn-sử-dụng)
7. [Hướng dẫn build và sử dụng Proto](#hướng-dẫn-build-và-sử-dụng-proto)

## Tổng quan về hệ thống

Đây là một hệ thống microservices được xây dựng bằng Spring Boot và gRPC, bao gồm các service chính:
- Eureka Server (Service Discovery)
- Account Service
- Fraud Detection Service
- Transaction Service
- Proto Module

## Kiến trúc hệ thống

### Mô hình giao tiếp
```
Client -> Transaction Service -> Account Service
                           -> Fraud Detection Service
```

### Các thành phần chính

1. **Eureka Server (Service Discovery)**
   - Port: 8762
   - Vai trò: Quản lý service discovery cho toàn bộ hệ thống
   - Sử dụng Spring Cloud Netflix Eureka Server
   - Cấu hình đơn giản với Spring Boot 3.4.4

2. **Account Service**
   - Port: 9595
   - Vai trò: Quản lý thông tin tài khoản
   - Sử dụng Spring Data JPA với PostgreSQL
   - Tích hợp gRPC và Eureka Client

3. **Fraud Detection Service**
   - Port: 9696
   - Vai trò: Phát hiện gian lận
   - Tương tự Account Service về mặt công nghệ
   - Tích hợp với Eureka và sử dụng gRPC

4. **Transaction Service**
   - Port: 9494
   - Vai trò: Xử lý các giao dịch
   - Sử dụng gRPC và Eureka Client

5. **Proto Module**
   - Chứa các định nghĩa gRPC proto files
   - Được chia sẻ giữa các service

## Công nghệ sử dụng

- **Spring Boot 3.4.4**
- **Spring Cloud 2024.0.1**
- **Java 21**
- **gRPC**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Maven**

## Cài đặt và triển khai

### Yêu cầu hệ thống
- Docker và Docker Compose
- Java 21
- Maven

### Các bước triển khai

1. Clone repository
2. Chạy lệnh sau để khởi động toàn bộ hệ thống:
```bash
docker-compose up -d
```

### Cấu hình Docker
- PostgreSQL database
- Các service Java được containerized
- Network bridge để các container giao tiếp
- Volume cho persistent data

## Cấu trúc các service

### Eureka Server
- Đơn giản và nhẹ
- Chỉ có chức năng service discovery
- Port mặc định: 8762

### Account Service
- Quản lý thông tin tài khoản
- Sử dụng JPA để tương tác với database
- Port: 9595

### Fraud Detection Service
- Xử lý logic phát hiện gian lận
- Port: 9696

### Transaction Service
- Xử lý các giao dịch
- Port: 9494

## Hướng dẫn sử dụng

### Kiểm tra trạng thái hệ thống
1. Truy cập Eureka Dashboard: http://localhost:8762
2. Kiểm tra các service đã đăng ký

### API Endpoints
- Account Service: http://localhost:9595
- Fraud Detection Service: http://localhost:9696
- Transaction Service: http://localhost:9494

### Monitoring
- Eureka Dashboard cho service discovery
- PostgreSQL cho database monitoring

### Troubleshooting
1. Kiểm tra logs của từng service:
```bash
docker logs [container_name]
```

2. Kiểm tra kết nối database:
```bash
docker exec -it postgres_db psql -U postgres
```

3. Restart service nếu cần:
```bash
docker-compose restart [service_name]
```

## Hướng dẫn build và sử dụng Proto

### Cấu trúc Proto Module
Proto module chứa các định nghĩa gRPC service và message được chia sẻ giữa các service. Cấu trúc thư mục:
```
proto/
├── src/
│   └── main/
│       └── proto/
│           ├── account.proto
│           ├── fraud.proto
│           └── transaction.proto
└── pom.xml
```

### Build Proto Files
1. Di chuyển vào thư mục proto:
```bash
cd proto
```

2. Build proto module:
```bash
./mvnw clean install
```

3. Kiểm tra kết quả build trong thư mục target:
```bash
ls target/generated-sources/protobuf
```

### Sử dụng Proto trong các Service
1. Thêm dependency vào pom.xml của service:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>proto</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

2. Import proto files trong service:
```java
import com.example.proto.AccountServiceGrpc;
import com.example.proto.AccountRequest;
import com.example.proto.AccountResponse;
```

### Cập nhật Proto Files
1. Chỉnh sửa các file .proto trong thư mục src/main/proto
2. Build lại proto module
3. Cập nhật version trong pom.xml của các service sử dụng proto
4. Build lại các service

### Lưu ý quan trọng
- Luôn build proto module trước khi build các service
- Đảm bảo version của proto module trong các service là đồng nhất
- Kiểm tra compatibility khi cập nhật proto files
- Backup proto files trước khi thực hiện thay đổi lớn

### Troubleshooting Proto
1. Lỗi build proto:
```bash
./mvnw clean install -X
```

2. Kiểm tra generated code:
```bash
find target -name "*.java"
```

3. Xóa cache Maven nếu cần:
```bash
rm -rf ~/.m2/repository/com/example/proto
``` 