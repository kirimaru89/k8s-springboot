# Luồng Xử Lý Giao Dịch

## 1. Luồng Thành Công (Happy Flow)

### 1.1 Khởi Tạo Giao Dịch
- Client gọi API: `POST /api/v1/digital-documents/library/saga/start-transaction`
- Camel Orchestrator bắt đầu một saga mới với `SagaPropagation.REQUIRED`
- Lưu trữ thông tin giao dịch ban đầu

### 1.2 Tạo Giao Dịch (Transaction Service)
- Tạo bản ghi giao dịch mới trong hệ thống
- Lưu trữ transactionId cho các bước tiếp theo
- Nếu thành công, chuyển sang bước tiếp theo
- Nếu thất bại, rollback saga

### 1.3 Cập Nhật Số Dư Tài Khoản (Account Service)
- Cập nhật số dư tài khoản dựa trên số tiền giao dịch
- Sử dụng transactionId từ bước trước
- Nếu thành công, chuyển sang bước tiếp theo
- Nếu thất bại, kích hoạt rollback trạng thái giao dịch

### 1.4 Kiểm Tra Gian Lận (Fraud Detection Service)
- Thực hiện phân tích phát hiện gian lận
- Kiểm tra mẫu giao dịch và các yếu tố rủi ro
- Nếu không phát hiện gian lận:
  - Hoàn tất cập nhật số dư tài khoản
  - Hoàn tất cập nhật trạng thái giao dịch
  - Đánh dấu giao dịch thành công
- Nếu phát hiện gian lận:
  - Kích hoạt quy trình rollback

## 2. Luồng Thất Bại và Rollback

### 2.1 Thất Bại Tạo Giao Dịch
- Nếu Transaction Service không thể tạo giao dịch
- Saga tự động rollback
- Không thực hiện các bước tiếp theo

### 2.2 Thất Bại Cập Nhật Số Dư
- Nếu Account Service không thể cập nhật số dư
- Kích hoạt `rollbackUpdateTransactionStatus`
- Cập nhật trạng thái giao dịch thành thất bại
- Saga hoàn tất với trạng thái thất bại

### 2.3 Thất Bại Phát Hiện Gian Lận
- Nếu phát hiện gian lận:
  - Kích hoạt `rollbackUpdateAccountBalance`
  - Hoàn tác số dư tài khoản
  - Kích hoạt `rollbackUpdateTransactionStatus`
  - Đánh dấu giao dịch thất bại do gian lận
- Nếu Fraud Service thất bại:
  - Thực hiện rollback tương tự như phát hiện gian lận
  - Hoàn tác số dư tài khoản
  - Cập nhật trạng thái giao dịch thành thất bại

## 3. Các Thành Phần Chính và Vai Trò

### 3.1 Camel Orchestrator
- Quản lý luồng saga
- Điều phối các service
- Triển khai logic rollback
- Xử lý lỗi

### 3.2 Transaction Service
- Tạo và quản lý bản ghi giao dịch
- Cập nhật trạng thái giao dịch
- Xử lý rollback giao dịch

### 3.3 Account Service
- Quản lý số dư tài khoản
- Xử lý cập nhật và rollback số dư
- Cung cấp các thao tác liên quan đến tài khoản

### 3.4 Fraud Detection Service
- Phân tích giao dịch để phát hiện gian lận
- Cung cấp kết quả phát hiện gian lận
- Có thể kích hoạt rollback giao dịch

## 4. Cơ Chế Bảo Đảm Tính Nhất Quán

- Sử dụng mẫu saga của Apache Camel để đảm bảo tính nhất quán giao dịch
- Mỗi bước là một phần của saga
- Nếu bất kỳ bước nào thất bại, các bước rollback tương ứng được thực thi
- Duy trì tính nhất quán dữ liệu giữa các service

## 5. Xử Lý Lỗi

- Mỗi service có cơ chế xử lý lỗi riêng
- Lỗi được bắt và xử lý ở mỗi bước
- Thông tin lỗi được ghi lại và truyền tải qua các bước rollback
- Đảm bảo trạng thái cuối cùng của hệ thống luôn nhất quán 