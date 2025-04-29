# Deploy with Docker compose
```shell
docker compose up --build -d
```

## Chuẩn bị

- request: tạo account mới cho user với userId = 19
```shell
curl --location 'localhost:8585/api/v1/digital-documents/library/accounts' \
--header 'Content-Type: application/json' \
--data '{
    "userId": 19
}'
```

- response: 
```json
{"code":200,"message":"Success","data":{"id":1,"userId":19,"balance":0.0}} 
```

- khi khởi động, database trống. account id được đánh số từ 1

## 1. Happy case

- request: Deposit với số tiền nhỏ hơn hoặc bằng 10.000
```shell
curl --location 'localhost:8484/api/v1/digital-documents/library/transactions' \
--header 'Content-Type: application/json' \
--data '{
    "accountId": 1,
    "amount": 10.01,
    "type": "DEPOSIT"
}'
```

- response:
```json
{"code":201,"message":"Created","data":{"id":2,"accountId":1,"amount":10.01,"type":"DEPOSIT","status":"INITIATED"}}
```
- topics:
  - transaction.initiated.v1
  - account.balance.reserved.v1
  - fraud.transaction.verified.v1
  - account.balance.completed.v1
  
## 2. Failure 1: rollback khi update account balance gặp lỗi

- request: withdraw với số tiền lớn hơn account có
```shell
curl --location 'localhost:8484/api/v1/digital-documents/library/transactions' \
--header 'Content-Type: application/json' \
--data '{
    "accountId": 1,
    "amount": 10000000.01,
    "type": "WITHDRAW"
}'
```

- response:
```json
{"code":201,"message":"Created","data":{"id":2,"accountId":1,"amount":10.01,"type":"DEPOSIT","status":"INITIATED"}}
```

- topics:
    - transaction.initiated.v1
    - account.balance.rollback.v1

## 3. Failure 2: rollback khi phát hiện gian lận

- request: deposit với số tiền lớn hơn 10.000
```shell
curl --location 'localhost:8484/api/v1/digital-documents/library/transactions' \
--header 'Content-Type: application/json' \
--data '{
    "accountId": 1,
    "amount": 10000000.01,
    "type": "DEPOSIT"
}' 
```

- response:
```json
{"code":201,"message":"Created","data":{"id":2,"accountId":1,"amount":10.01,"type":"DEPOSIT","status":"INITIATED"}}
```
- topics:
    - transaction.initiated.v1
    - account.balance.reserved.v1
    - fraud.transaction.detected.v1
    - account.balance.rollback.v1
