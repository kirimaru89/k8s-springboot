# Hệ Thống Microservices trên Kubernetes

## Tổng quan

Dự án triển khai một hệ thống microservices trên Kubernetes, sử dụng Spring Boot cho các ứng dụng và tích hợp nhiều công nghệ hiện đại như Kafka, Redis, Prometheus, Grafana, Tempo, OpenTelemetry, và HashiCorp Vault.

## Cấu trúc Dự án

Dự án được chia thành hai thư mục chính:

### 1. Thư mục `demo`
Chứa các ứng dụng Spring Boot chính (các ứng dụng đều có logging, metrics & tracing)

- **spring-app-1**: API Gateway/Service
  - Vai trò: Cổng giao tiếp chính cho hệ thống
  - Cấu hình: Sử dụng Vault, Circuit breaker

- **spring-app-2**: Service xử lý nghiệp vụ
  - Vai trò: Xử lý các nghiệp vụ chính của hệ thống
  - Tính năng: Circuit breaker

- **spring-app-3**: Service gửi request (Producer)
  - Vai trò: Gửi yêu cầu qua Kafka
  - Tích hợp: Request/Reply pattern với spring-app-4

- **spring-app-4**: Service xử lý và trả lời request (Consumer + Producer)
  - Vai trò: Nhận, xử lý và trả lời yêu cầu
  - Tích hợp: Request/Reply pattern với spring-app-3

### 2. Thư mục `projecttemplate`
Chứa các ứng dụng Kafka (các ứng dụng đều có logging, metrics & tracing):

- **com.vietinbank.kproducer**: Producer Kafka
  - Vai trò: Gửi tin nhắn vào Kafka
  - Tích hợp: Redis, Kafka

- **com.vietinbank.kconsumer**: Consumer Kafka
  - Vai trò: Nhận và xử lý tin nhắn từ Kafka
  - Tích hợp: Kafka

## Kiến trúc Hệ thống

### Giao tiếp giữa các Service

#### Giao tiếp Đồng bộ
- REST APIs giữa các service
- Truy cập cơ sở dữ liệu (MySQL/PostgreSQL)
- Sử dụng Redis cho caching

#### Giao tiếp Bất đồng bộ (Event-driven)
- Kafka Topics:
  - Request/Reply pattern giữa `spring-app-3` và `spring-app-4`
  - Message streaming từ `com.vietinbank.kproducer` đến `com.vietinbank.kconsumer`

### Cơ sở hạ tầng

#### Message Broker
- Kafka (với Zookeeper)
- Version: 18.0.1
- Namespace: monitoring
- Topics được cấu hình cho request/reply pattern

#### Cơ sở dữ liệu
- MySQL
- PostgreSQL

#### Caching
- Redis

#### Observability Stack
- **Prometheus**: Thu thập metrics từ tất cả services
- **Grafana**: Hiển thị dashboards cho monitoring
- **Tempo**: Distributed tracing
- **OpenTelemetry Collector**: Thu thập và chuyển tiếp traces và metrics

#### Logging Stack (ELK)
- **Elasticsearch**: Lưu trữ logs
- **Logstash**: Xử lý và transform logs
- **Kibana**: Visualization
- **Filebeat**: Thu thập logs từ containers

#### Bảo mật
- **HashiCorp Vault**: Quản lý bí mật
  - Thông tin đăng nhập cơ sở dữ liệu
  - Khóa API
  - Token dịch vụ
  - Vault Agent Injector
  - Xác thực Kubernetes
  - Kiểm soát truy cập dựa trên chính sách

#### Khả năng phục hồi
- **Circuit Breaker (Resilience4j)**:
  - Kích thước cửa sổ trượt: 8-10
  - Ngưỡng tỷ lệ lỗi: 20-50%
  - Thời gian chờ: 10-60 giây
  - Số lần gọi ở trạng thái nửa mở: 3
  - Bật theo dõi chỉ số

## Triển khai và Vận hành

### Triển khai Kubernetes
- Deployments cho mỗi dịch vụ
- ConfigMaps cho cấu hình
- Secrets cho dữ liệu nhạy cảm

### Giám sát và Cảnh báo
- Cảnh báo Prometheus
- Bảng điều khiển Grafana
- Tổng hợp log
- Theo dõi phân tán

## Hướng dẫn Sử dụng

### Yêu cầu Hệ thống
- Kubernetes cluster (Kind, Minikube, hoặc cloud provider)
- Helm
- Docker
- kubectl

## Giám sát và Gỡ lỗi

### Metrics và Tracing
- Truy cập Grafana: `http://localhost:3000`
- Xem traces: `http://localhost:3000/explore?orgId=1&left={"datasource":"Tempo"}`

### Logs
- Truy cập Kibana: `http://localhost:5601`

### Kiểm tra Trạng thái
```bash
# Kiểm tra trạng thái pods
kubectl get pods

# Xem logs của một pod
kubectl logs <pod-name>

# Mô tả pod để xem chi tiết
kubectl describe pod <pod-name>
```

## Tài liệu Tham khảo

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [HashiCorp Vault Documentation](https://www.vaultproject.io/docs)
- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [Resilience4j Documentation](https://resilience4j.readme.io/docs)


### 3. Thư mục `gRPC`
Chứa thông tin demo gPRC. Cụ thể xem trong file gRPC\README.md và gRPC\architecture.md

### 4. Thư mục `saga\orchestration`
Chứa thông tin demo Saga pattern Orchestration. Cụ thể xem trong file saga\orchestration\README.md và saga\orchestration\architecture.md

### 5. Thư mục `saga\choreography`
Chứa thông tin demo Saga pattern Choreography. Cụ thể xem trong file saga\choreography\README.md và saga\choreography\architecture.md