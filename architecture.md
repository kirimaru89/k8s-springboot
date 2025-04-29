# Kiến trúc Hệ thống Microservices trên Kubernetes

## 1. Tổng quan Hệ thống

### 1.1. Các thành phần chính
- **Spring Boot Applications**: 6 ứng dụng microservice
  - `spring-app-1`: API Gateway/Service
  - `spring-app-2`: Service xử lý nghiệp vụ
  - `spring-app-3`: Service gửi request (Producer)
  - `spring-app-4`: Service xử lý và trả lời request (Consumer + Producer)
  - `com.vietinbank.kproducer`: Producer Kafka
  - `com.vietinbank.kconsumer`: Consumer Kafka

### 1.2. Cơ sở hạ tầng
- **Message Broker**: Kafka (với Zookeeper)
- **Databases**: 
  - MySQL
  - PostgreSQL
  - Oracle (development)
- **Caching**: Redis
- **Observability Stack**:
  - Prometheus (metrics)
  - Grafana (visualization)
  - Tempo (distributed tracing)
  - OpenTelemetry Collector
- **Logging Stack (ELK)**:
  - Elasticsearch
  - Logstash
  - Kibana
  - Filebeat
- **Security**: HashiCorp Vault
- **Resilience**: Circuit Breaker (Resilience4j)

## 2. Luồng Dữ liệu và Giao tiếp

### 2.1. Giao tiếp Đồng bộ
- REST APIs giữa các service
- Database access (MySQL/PostgreSQL)
- Redis caching

### 2.2. Giao tiếp Bất đồng bộ (Event-driven)
- Kafka Topics:
  - Request/Reply pattern giữa `spring-app-3` và `spring-app-4`
  - Message streaming từ `com.vietinbank.kproducer` đến `com.vietinbank.kconsumer`

## 3. Chi tiết Các Service

### 3.1. spring-app-1
- **Vai trò**: API Gateway/Service
- **Cấu hình**:
  - Sử dụng Vault để quản lý secrets
  - Circuit breaker cho fault tolerance
  - OpenTelemetry cho tracing

### 3.2. spring-app-2
- **Vai trò**: Service xử lý nghiệp vụ
- **Tính năng**:
  - Xử lý nghiệp vụ chính
  - Tích hợp với database
  - Circuit breaker cho các external calls

### 3.3. spring-app-3 & spring-app-4
- **Mô hình Request/Reply**:
  - `spring-app-3`: Gửi request (Producer)
  - `spring-app-4`: Xử lý và trả lời (Consumer + Producer)
- **Kafka Topics**:
  - Request Topic: `request-topic`
  - Reply Topic: `reply-topic`

### 3.4. com.vietinbank.kproducer & com.vietinbank.kconsumer
- **Vai trò**: Xử lý message streaming
- **Kafka Integration**:
  - Producer: Gửi messages
  - Consumer: Xử lý messages

## 4. Cơ sở hạ tầng

### 4.1. Kafka Setup
- Deployed via Helm chart
- Version: 18.0.1
- Namespace: monitoring
- Topics được cấu hình cho request/reply pattern

### 4.2. Observability Stack
- **Prometheus**:
  - Scraping metrics từ tất cả services
  - Custom metrics cho circuit breaker
  - JVM metrics
- **Grafana**:
  - Dashboards cho monitoring
  - Integration với Tempo cho tracing
- **Tempo**:
  - Distributed tracing
  - Integration với Grafana
- **OpenTelemetry Collector**:
  - Thu thập traces và metrics
  - Forwarding đến Tempo và Prometheus

### 4.3. Logging Stack (ELK)
- **Elasticsearch**: Lưu trữ logs
- **Logstash**: Xử lý và transform logs
- **Kibana**: Visualization
- **Filebeat**: Log collection từ containers

### 4.4. Bảo mật (Vault)
- **Quản lý bí mật**:
  - Thông tin đăng nhập cơ sở dữ liệu
  - Khóa API
  - Token dịch vụ
- **Tích hợp**:
  - Vault Agent Injector
  - Xác thực Kubernetes
  - Kiểm soát truy cập dựa trên chính sách

### 4.5. Khả năng phục hồi
- **Cấu hình Circuit Breaker**:
  - Kích thước cửa sổ trượt: 8-10
  - Ngưỡng tỷ lệ lỗi: 20-50%
  - Thời gian chờ: 10-60 giây
  - Số lần gọi ở trạng thái nửa mở: 3
  - Bật theo dõi chỉ số

### 5. Triển khai và Vận hành
- **Ảnh Container**:
  - Xây dựng với Docker BuildKit
  - Nền tảng: linux/arm64
  - Ảnh nền: Spring Boot
  - Xây dựng nhiều giai đoạn (Multi-stage builds)
- **Triển khai Kubernetes**:
  - Deployments cho mỗi dịch vụ
  - ConfigMaps cho cấu hình
  - Secrets cho dữ liệu nhạy cảm
  - Tài khoản dịch vụ và RBAC
- **Giám sát và Cảnh báo**:
  - Cảnh báo Prometheus
  - Bảng điều khiển Grafana
  - Tổng hợp log
  - Theo dõi phân tán