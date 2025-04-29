# Hướng dẫn sử dụng hệ thống Saga Choreography

## 1. Tổng quan về Kiến trúc Dự án

### 1.1. Cấu trúc Module
Dự án được chia thành 3 microservice chính:
- **Account Service (Port: 8585)**: Quản lý tài khoản và số dư
- **Transaction Service (Port: 8484)**: Xử lý các giao dịch
- **Fraud Detection Service (Port: 8686)**: Kiểm tra gian lận

### 1.2. Kiến trúc Phân lớp
Mỗi service được tổ chức theo mô hình phân lớp:
- **Controller Layer**: Xử lý HTTP requests
- **Service Layer**: Logic nghiệp vụ
- **Repository Layer**: Tương tác với database
- **Event Layer**: Xử lý các sự kiện Kafka
- **Model Layer**: Định nghĩa các entity và DTO

### 1.3. Các Service Tham Gia
1. **Account Service**:
   - Quản lý tài khoản người dùng
   - Cập nhật số dư
   - Xử lý các sự kiện liên quan đến giao dịch

2. **Transaction Service**:
   - Khởi tạo giao dịch
   - Điều phối luồng giao dịch
   - Xử lý các trạng thái giao dịch

3. **Fraud Detection Service**:
   - Kiểm tra tính hợp lệ của giao dịch
   - Phát hiện gian lận
   - Xác thực giao dịch

## 2. Cấu Hình và Cài Đặt

### 2.1. Công Cụ Build
- Sử dụng Maven làm công cụ build
- Cấu trúc POM file chuẩn Spring Boot
- Java version: 21

### 2.2. Cấu Hình Môi Trường
#### Docker Compose
```yaml
version: '3.8'
services:
  - postgres: Database chính
  - zookeeper: Quản lý Kafka
  - kafka: Message broker
  - kafka-ui: Giao diện quản lý Kafka
  - account1: Account Service
  - transaction1: Transaction Service
  - fraud1: Fraud Detection Service
```

### 2.3. Cấu Hình Database
- PostgreSQL
- Port: 5432
- Username: postgres
- Password: postgres

### 2.4. Cấu Hình Kafka
- Bootstrap servers: kafka:9092
- Topics:
  - transaction.initiated.v1
  - account.balance.reserved.v1
  - fraud.transaction.verified.v1
  - account.balance.completed.v1
  - account.balance.rollback.v1
  - fraud.transaction.detected.v1

## 3. Hướng Dẫn Chạy Dự Án

### 3.1. Khởi Động Hệ Thống
```bash
docker compose up --build -d
```

### 3.2. Kiểm Tra Các Service
- Account Service: http://localhost:8585
- Transaction Service: http://localhost:8484
- Fraud Detection Service: http://localhost:8686
- Kafka UI: http://localhost:8082

### 3.3. Các API Endpoint Chính

#### Tạo Tài Khoản
```bash
curl --location 'localhost:8585/api/v1/digital-documents/library/accounts' \
--header 'Content-Type: application/json' \
--data '{
    "userId": 19
}'
```

#### Thực Hiện Giao Dịch
```bash
curl --location 'localhost:8484/api/v1/digital-documents/library/transactions' \
--header 'Content-Type: application/json' \
--data '{
    "accountId": 1,
    "amount": 10.01,
    "type": "DEPOSIT"
}'
```

## 4. Luồng Xử Lý Saga

### 4.1. Happy Path (Giao Dịch Thành Công)
1. Transaction Service khởi tạo giao dịch
2. Account Service kiểm tra và đặt trước số dư
3. Fraud Detection Service xác thực giao dịch
4. Account Service hoàn tất giao dịch

### 4.2. Failure Paths (Xử Lý Lỗi)
1. **Lỗi Số Dư Không Đủ**:
   - Transaction Service khởi tạo giao dịch
   - Account Service phát hiện số dư không đủ
   - Hệ thống rollback giao dịch

2. **Lỗi Phát Hiện Gian Lận**:
   - Transaction Service khởi tạo giao dịch
   - Account Service đặt trước số dư
   - Fraud Detection Service phát hiện gian lận
   - Hệ thống rollback giao dịch

## 5. Cấu Hình Quan Trọng

### 5.1. Spring Configuration
- Transaction Management
- Event Publishing
- Kafka Configuration
- Database Configuration

### 5.2. Kafka Topics
- Các topic được cấu hình tự động
- Replication factor: 1
- Auto create topics: enabled

## 6. Xử Lý Sự Cố Thường Gặp

### 6.1. Kết Nối Database
- Kiểm tra PostgreSQL container
- Xác nhận thông tin kết nối
- Kiểm tra volume persistence

### 6.2. Kafka Issues
- Kiểm tra Zookeeper status
- Xác nhận Kafka broker
- Kiểm tra topic creation

### 6.3. Service Communication
- Kiểm tra network configuration
- Xác nhận service discovery
- Kiểm tra health checks