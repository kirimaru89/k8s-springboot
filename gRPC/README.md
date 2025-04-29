# I. Đóng gói protobuffer

```shell
cd proto
mvn clean install -DskipTests
```

# II. Deploy with Docker compose
```shell

docker compose up --build -d
```

## 1. Chuẩn bị

### 1.0. Cài đặt grpcurl cli (hoặc sử dụng postman)
- trên mac:
```shell
brew install grpcurl 
```

- trên win:
```text
download: [link](https://github.com/fullstorydev/grpcurl/releases)
```

- trên linux:
```shell
curl -LO https://github.com/fullstorydev/grpcurl/releases/download/v1.8.1/grpcurl_1.8.1_linux_x86_64.tar.gz
tar -xvzf grpcurl_1.8.1_linux_x86_64.tar.gz
sudo mv grpcurl /usr/local/bin/

```

- kiểm tra:
```shell
grpcurl --version
```

### 1.1. Tạo account mới
- request: tạo account mới cho user với userId = 19
```shell
grpcurl --plaintext -d '{"user_id": "19"}' \
localhost:9595 digitaldocuments.library.v1.AccountService/CreateAccount
```

- response:
```json
{
  "id": "1",
  "user_id": "19",
  "balance": 0
}
```

- khi khởi động, database trống. account id được đánh số từ 1

## 2. Happy case

- request: Deposit với số tiền nhỏ hơn hoặc bằng 10.000
```shell
grpcurl --plaintext -d '{"account_id":1,"amount":10.01,"type":"DEPOSIT"}' \
localhost:9494 digitaldocuments.library.v1.TransactionService/CreateTransaction
```

- response:
```json
{
  "id": "1",
  "account_id": "1",
  "amount": 10.01,
  "type": "DEPOSIT"
}
```

## 3. Failure 1: rollback khi update account balance gặp lỗi

- request: withdraw với số tiền lớn hơn account có (nhỏ hơn 10000) để tránh fraud detection
```shell
grpcurl --plaintext -d '{"account_id":"1","amount":1000.01,"type":"WITHDRAW"}' \
localhost:9494 digitaldocuments.library.v1.TransactionService/CreateTransaction
```

- response:
```text
ERROR:
  Code: Internal
  Message: INVALID_ARGUMENT: [AccountService] Insufficient funds. Transaction aborted.
```

## 4. Failure 2: rollback khi phát hiện gian lận

- request: deposit với số tiền lớn hơn 10.000
```shell
grpcurl --plaintext -d '{"account_id":"1","amount":100000.01,"type":"DEPOSIT"}' \
localhost:9494 digitaldocuments.library.v1.TransactionService/CreateTransaction
```

- response:
```text
ERROR:
  Code: Internal
  Message: INVALID_ARGUMENT: [FraudDetectionService] Error: fraud detected
```
