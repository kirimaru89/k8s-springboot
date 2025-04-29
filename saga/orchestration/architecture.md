# Hướng dẫn sử dụng Saga Orchestration Project

## 1. Tổng quan về Kiến trúc Dự án

### 1.1. Cấu trúc Module
Dự án được chia thành 4 module chính:
- **camel-orchestration**: Module điều phối Saga, quản lý luồng giao dịch
- **account-service**: Service quản lý tài khoản và số dư
- **transaction-service**: Service xử lý giao dịch
- **fraud-detection-service**: Service phát hiện gian lận

### 1.2. Các Layer trong Mỗi Module
Mỗi module đều tuân theo cấu trúc layer chuẩn:
- **Controller**: Xử lý HTTP requests
- **Service**: Logic nghiệp vụ
- **Repository**: Tương tác với database
- **DTO**: Data Transfer Objects
- **Config**: Cấu hình ứng dụng
- **Exception**: Xử lý lỗi

### 1.3. Saga Orchestrator
Module `camel-orchestration` đóng vai trò là Saga Orchestrator, với các thành phần chính:
- **Routes**: Định nghĩa các Camel routes cho luồng Saga
- **Controller**: API endpoints để khởi tạo và theo dõi Saga
- **Config**: Cấu hình Camel và các service clients

### 1.4. Các Service Tham Gia
1. **Account Service**:
   - Quản lý tài khoản người dùng
   - Cập nhật số dư
   - Xử lý rollback khi cần

2. **Transaction Service**:
   - Ghi nhận giao dịch
   - Xử lý các loại giao dịch (deposit/withdraw)
   - Quản lý trạng thái giao dịch

3. **Fraud Detection Service**:
   - Kiểm tra tính hợp lệ của giao dịch
   - Phát hiện gian lận
   - Báo cáo kết quả kiểm tra

## 2. Cấu hình và Build Project

### 2.1. Công cụ Build
- Sử dụng Maven làm công cụ build
- Mỗi module có file `pom.xml` riêng
- Java version: 21

### 2.2. Cấu hình Môi trường
Dự án sử dụng Docker Compose để cấu hình môi trường, bao gồm:
- PostgreSQL database
- Các service containers
- Network configuration

### 2.3. Docker Compose
File `docker-compose.yml` định nghĩa:
- PostgreSQL container
- 4 service containers (account, transaction, fraud, orchestrator)
- Volume mappings
- Network configuration
- Environment variables

## 3. Hướng dẫn Chạy Project

### 3.1. Khởi động Môi trường
```bash
docker compose up --build -d
```

### 3.2. Các API Endpoints

#### Tạo tài khoản mới
```bash
curl --location 'localhost:8484/api/v1/digital-documents/library/accounts' \
--header 'Content-Type: application/json' \
--data '{
    "userId": 19
}'
```

#### Thực hiện giao dịch
```bash
curl --location 'localhost:8787/api/v1/digital-documents/library/saga/start-transaction' \
--header 'Content-Type: application/json' \
--data '{
    "accountId": 1,
    "amount": 10.01,
    "type": "DEPOSIT"
}'
```

## 4. Luồng Xử lý Saga

### 4.1. Happy Path (Giao dịch thành công)
1. Khởi tạo giao dịch (Deposit ≤ 10,000)
2. Kiểm tra gian lận
3. Cập nhật số dư tài khoản
4. Ghi nhận giao dịch
5. Hoàn thành Saga

### 4.2. Failure Scenarios
1. **Lỗi số dư không đủ**:
   - Trigger: Withdraw với số tiền > số dư
   - Rollback: Không thực hiện giao dịch

2. **Lỗi phát hiện gian lận**:
   - Trigger: Deposit > 10,000
   - Rollback: Hủy giao dịch

## 5. Cấu hình Quan trọng

### 5.1. Database
- PostgreSQL
- Port: 5432
- Username: postgres
- Password: postgres

### 5.2. Service Ports
- Account Service: 8484
- Transaction Service: 8585
- Fraud Service: 8686
- Orchestrator: 8787

## 6. Xử lý Sự cố Thường gặp

### 6.1. Connection Refused
- Kiểm tra các service đã khởi động
- Verify network configuration
- Kiểm tra port mapping

### 6.2. Database Issues
- Verify PostgreSQL container status
- Kiểm tra connection string
- Xem logs của service gặp lỗi