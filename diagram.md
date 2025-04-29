# Sơ đồ Luồng Dữ liệu và Tương tác Hệ thống

## 1. Tổng quan Kiến trúc Hệ thống

```mermaid
graph TB
    subgraph "Ứng dụng Spring Boot"
        app1[spring-app-1<br/>Cổng API]
        app2[spring-app-2<br/>Dịch vụ Nghiệp vụ]
        app3[spring-app-3<br/>Bộ phát Yêu cầu]
        app4[spring-app-4<br/>Xử lý Yêu cầu]
        kproducer[com.vietinbank.kproducer<br/>Bộ phát Kafka]
        kconsumer[com.vietinbank.kconsumer<br/>Bộ thu Kafka]
    end

    subgraph "Hệ thống Message Broker"
        kafka[Kafka]
        zk[Zookeeper]
    end

    subgraph "Cơ sở dữ liệu"
        mysql[(MySQL)]
        postgres[(PostgreSQL)]
        oracle[(Oracle)]
    end

    subgraph "Bộ nhớ đệm"
        redis[(Redis)]
    end

    subgraph "Hệ thống Giám sát"
        prometheus[Prometheus]
        grafana[Grafana]
        tempo[Tempo]
        otel[Bộ thu thập<br/>OpenTelemetry]
    end

    subgraph "Hệ thống Ghi log"
        es[Elasticsearch]
        logstash[Logstash]
        kibana[Kibana]
        filebeat[Filebeat]
    end

    subgraph "Bảo mật"
        vault[HashiCorp Vault]
    end

    %% Kết nối
    app1 --> app2
    app1 --> app3
    app2 --> mysql
    app2 --> postgres
    app3 --> kafka
    app4 --> kafka
    kproducer --> kafka
    kafka --> kconsumer
    kafka --> zk
    app1 --> redis
    app2 --> redis
    
    %% Kết nối giám sát
    app1 --> otel
    app2 --> otel
    app3 --> otel
    app4 --> otel
    kproducer --> otel
    kconsumer --> otel
    otel --> prometheus
    otel --> tempo
    prometheus --> grafana
    tempo --> grafana

    %% Kết nối ghi log
    filebeat --> logstash
    logstash --> es
    es --> kibana

    %% Kết nối bảo mật
    app1 --> vault
    app2 --> vault
    app3 --> vault
    app4 --> vault
```

## 2. Luồng Yêu cầu/Phản hồi qua Kafka

```mermaid
sequenceDiagram
    participant Client as Máy khách
    participant App3 as spring-app-3
    participant Kafka
    participant App4 as spring-app-4

    Client->>App3: Yêu cầu HTTP
    App3->>Kafka: Gửi đến chủ đề yêu cầu
    Note over App3,Kafka: Bản ghi với ID tương quan
    Kafka->>App4: Nhận từ chủ đề yêu cầu
    Note over App4: Xử lý yêu cầu
    App4->>Kafka: Gửi đến chủ đề phản hồi
    Note over App4,Kafka: Bao gồm ID tương quan
    Kafka->>App3: Nhận từ chủ đề phản hồi
    Note over App3: Khớp ID tương quan
    App3->>Client: Phản hồi HTTP
```

## 3. Luồng Truyền Phát Tin nhắn

```mermaid
sequenceDiagram
    participant Producer as Bộ phát
    participant Kafka
    participant Consumer as Bộ thu
    participant DB as Cơ sở dữ liệu

    Producer->>Kafka: Gửi tin nhắn
    Note over Producer,Kafka: Chủ đề: streaming-topic
    Kafka->>Consumer: Nhận tin nhắn
    Consumer->>DB: Xử lý & Lưu trữ
    Note over Consumer,DB: Xử lý giao dịch
```

## 4. Luồng Giám sát Hệ thống

```mermaid
graph LR
    subgraph "Ứng dụng"
        app1[spring-app-1]
        app2[spring-app-2]
        app3[spring-app-3]
        app4[spring-app-4]
    end

    subgraph "OpenTelemetry"
        otel[Bộ thu thập OpenTelemetry]
    end

    subgraph "Số liệu"
        prom[Prometheus]
    end

    subgraph "Theo dõi"
        tempo[Tempo]
    end

    subgraph "Hiển thị"
        graf[Grafana]
    end

    app1 --> otel
    app2 --> otel
    app3 --> otel
    app4 --> otel

    otel --> prom
    otel --> tempo

    prom --> graf
    tempo --> graf
```

## 5. Luồng Ghi Log

```mermaid
graph LR
    subgraph "Ứng dụng"
        app1[spring-app-1]
        app2[spring-app-2]
        app3[spring-app-3]
        app4[spring-app-4]
    end

    subgraph "Thu thập Log"
        fb[Filebeat]
    end

    subgraph "Xử lý Log"
        ls[Logstash]
    end

    subgraph "Lưu trữ Log"
        es[Elasticsearch]
    end

    subgraph "Hiển thị Log"
        kb[Kibana]
    end

    app1 --> fb
    app2 --> fb
    app3 --> fb
    app4 --> fb

    fb --> ls
    ls --> es
    es --> kb
```

## 6. Luồng Bảo mật với Vault

```mermaid
sequenceDiagram
    participant App as Ứng dụng Spring
    participant K8s as Kubernetes
    participant Vault
    participant Secret as Kho lưu trữ Bí mật

    App->>K8s: Yêu cầu với Tài khoản Dịch vụ
    K8s->>Vault: Xác thực với JWT
    Vault->>K8s: Xác thực Token
    Vault->>Secret: Lấy Bí mật
    Secret->>Vault: Trả về Bí mật
    Vault->>App: Tiêm Bí mật
    Note over App: /vault/secrets/
```

## 7. Luồng Ngắt Mạch Bảo vệ

```mermaid
stateDiagram-v2
    [*] --> Đóng
    Đóng --> Mở: Tỷ lệ lỗi > Ngưỡng
    Mở --> NửaMở: Hết thời gian chờ
    NửaMở --> Đóng: Thành công
    NửaMở --> Mở: Thất bại
    Đóng --> [*]: Dịch vụ ngừng hoạt động
    Mở --> [*]: Dịch vụ ngừng hoạt động
    NửaMở --> [*]: Dịch vụ ngừng hoạt động
``` 