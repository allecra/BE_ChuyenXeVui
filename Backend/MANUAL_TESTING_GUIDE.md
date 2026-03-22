# 🧪 Hướng dẫn Test Thủ công - Hệ thống Thanh toán

## 📋 Chuẩn bị

### 1. Khởi động ứng dụng

```bash
cd Backend
mvn spring-boot:run
```

### 2. Kiểm tra ứng dụng đã chạy

- Mở trình duyệt: http://localhost:8080
- Hoặc test endpoint: http://localhost:8080/api/payment/test/providers

---

## 🔧 Test 1: Kiểm tra Payment Providers

### Endpoint: GET /api/payment/test/providers

**Cách test:**

```bash
curl http://localhost:8080/api/payment/test/providers
```

**Kết quả mong đợi:**

```json
{
  "momo_enabled": true,
  "momo_name": "MOMO",
  "sepay_enabled": true,
  "sepay_name": "SEPAY",
  "total_providers": 2
}
```

**Ý nghĩa các thuộc tính:**

- `momo_enabled`: MoMo có hoạt động không (true/false)
- `momo_name`: Tên provider MoMo
- `sepay_enabled`: SePay có hoạt động không (true/false)
- `sepay_name`: Tên provider SePay
- `total_providers`: Tổng số provider có sẵn

---

## 🔐 Test 2: Đăng ký và Đăng nhập

### 2.1 Đăng ký user mới

**Endpoint:** POST /api/auth/register

**Cách test:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "password123",
    "phone": "0123456789"
  }'
```

**Ý nghĩa các thuộc tính:**

- `firstName`: Tên (bắt buộc)
- `lastName`: Họ (bắt buộc)
- `email`: Email (bắt buộc, unique)
- `password`: Mật khẩu (bắt buộc, tối thiểu 6 ký tự)
- `phone`: Số điện thoại (tùy chọn)

### 2.2 Đăng nhập

**Endpoint:** POST /api/auth/login

**Cách test:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Kết quả mong đợi:**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "email": "test@example.com"
    }
  }
}
```

**⚠️ LƯU JWT TOKEN để dùng cho các test tiếp theo:**

```
JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

---

## 💳 Test 3: Tạo thanh toán MoMo

### Endpoint: POST /api/user/payments

**Cách test:**

```bash
curl -X POST http://localhost:8080/api/user/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "ticketId": 1,
    "amount": 50000,
    "provider": "MOMO",
    "description": "Test thanh toán MoMo",
    "returnUrl": "http://localhost:3000/success"
  }'
```

**Ý nghĩa các thuộc tính:**

- `ticketId`: ID của vé cần thanh toán (bắt buộc)
- `amount`: Số tiền thanh toán (VND, bắt buộc)
- `provider`: Nhà cung cấp thanh toán ("MOMO" hoặc "SEPAY")
- `description`: Mô tả giao dịch (tùy chọn)
- `returnUrl`: URL trở về sau khi thanh toán (tùy chọn)

**Kết quả mong đợi:**

```json
{
  "success": true,
  "data": {
    "transactionId": "PAY_20260311_001",
    "provider": "MOMO",
    "amount": 50000.0,
    "status": "PENDING",
    "qrCodeUrl": "https://test-payment.momo.vn/qr/...",
    "paymentUrl": "https://test-payment.momo.vn/pay/...",
    "expiredAt": "2026-03-11T09:00:00"
  }
}
```

**⚠️ LƯU TRANSACTION_ID để test tiếp:**

```
TRANSACTION_ID="PAY_20260311_001"
```

---

## 🏦 Test 4: Tạo thanh toán SePay

**Cách test:**

```bash
curl -X POST http://localhost:8080/api/user/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "ticketId": 2,
    "amount": 75000,
    "provider": "SEPAY",
    "description": "Test chuyển khoản SePay"
  }'
```

**Kết quả mong đợi:**

```json
{
  "success": true,
  "data": {
    "transactionId": "PAY_20260311_002",
    "provider": "SEPAY",
    "amount": 75000.0,
    "status": "PENDING",
    "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "expiredAt": "2026-03-12T08:00:00"
  }
}
```

---

## 📊 Test 5: Kiểm tra trạng thái thanh toán

### Endpoint: GET /api/payment/status/{transactionId}

**Cách test:**

```bash
curl http://localhost:8080/api/payment/status/PAY_20260311_001
```

**Kết quả mong đợi:**

```json
{
  "success": true,
  "data": {
    "transactionId": "PAY_20260311_001",
    "provider": "MOMO",
    "amount": 50000.0,
    "status": "PENDING",
    "description": "Test thanh toán MoMo",
    "createdAt": "2026-03-11T08:00:00",
    "expiredAt": "2026-03-11T09:00:00"
  }
}
```

**Ý nghĩa các trạng thái:**

- `PENDING`: Chờ thanh toán
- `PROCESSING`: Đang xử lý
- `COMPLETED`: Thành công
- `FAILED`: Thất bại
- `CANCELLED`: Đã hủy
- `EXPIRED`: Hết hạn

---

## 📋 Test 6: Lấy danh sách thanh toán của user

### Endpoint: GET /api/user/payments

**Cách test:**

```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/user/payments
```

**Kết quả mong đợi:**

```json
{
  "success": true,
  "data": [
    {
      "transactionId": "PAY_20260311_002",
      "provider": "SEPAY",
      "amount": 75000.0,
      "status": "PENDING",
      "createdAt": "2026-03-11T08:05:00"
    },
    {
      "transactionId": "PAY_20260311_001",
      "provider": "MOMO",
      "amount": 50000.0,
      "status": "PENDING",
      "createdAt": "2026-03-11T08:00:00"
    }
  ]
}
```

---

## 🔍 Test 7: Lấy chi tiết thanh toán

### Endpoint: GET /api/user/payments/{transactionId}

**Cách test:**

```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/user/payments/PAY_20260311_001
```

**Kết quả mong đợi:**

```json
{
  "success": true,
  "data": {
    "transactionId": "PAY_20260311_001",
    "provider": "MOMO",
    "amount": 50000.0,
    "status": "PENDING",
    "qrCodeUrl": "https://test-payment.momo.vn/qr/...",
    "paymentUrl": "https://test-payment.momo.vn/pay/...",
    "userId": 1,
    "ticketId": 1
  }
}
```

---

## 🔄 Test 8: Callback MoMo (Giả lập)

### Endpoint: POST /api/payment/ipn/momo

**Cách test:**

```bash
curl -X POST http://localhost:8080/api/payment/ipn/momo \
  -H "Content-Type: application/json" \
  -d '{
    "partnerCode": "MOMO",
    "orderId": "PAY_20260311_001",
    "requestId": "PAY_20260311_001",
    "amount": 50000,
    "orderInfo": "Test thanh toán MoMo",
    "resultCode": 0,
    "message": "Successful.",
    "transId": 2889000000,
    "signature": "test_signature"
  }'
```

**Ý nghĩa các thuộc tính:**

- `partnerCode`: Mã đối tác MoMo
- `orderId`: Mã đơn hàng (= transactionId)
- `amount`: Số tiền
- `resultCode`: Kết quả (0 = thành công, khác 0 = thất bại)
- `message`: Thông báo kết quả
- `transId`: Mã giao dịch từ MoMo
- `signature`: Chữ ký xác thực

**Kết quả mong đợi:**

```json
{
  "resultCode": 0,
  "message": "Success"
}
```

---

## 🏦 Test 9: Callback SePay (Giả lập)

### Endpoint: POST /api/payment/ipn/sepay

**Cách test:**

```bash
curl -X POST http://localhost:8080/api/payment/ipn/sepay \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "SEPAY123456",
    "amount": 75000,
    "description": "Test chuyển khoản - Ma GD: PAY_20260311_002",
    "status": "success"
  }'
```

**Ý nghĩa các thuộc tính:**

- `transactionId`: Mã giao dịch từ SePay
- `amount`: Số tiền
- `description`: Mô tả (chứa mã giao dịch của hệ thống)
- `status`: Trạng thái ("success" hoặc "failed")

**Kết quả mong đợi:**

```json
{
  "success": true,
  "message": "Callback processed successfully"
}
```

---

## ❌ Test 10: Các trường hợp lỗi

### 10.1 Provider không tồn tại

```bash
curl -X POST http://localhost:8080/api/user/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "ticketId": 1,
    "amount": 50000,
    "provider": "VNPAY"
  }'
```

**Kết quả mong đợi:**

```json
{
  "success": false,
  "message": "Không tìm thấy nhà cung cấp thanh toán: VNPAY"
}
```

### 10.2 Ticket không tồn tại

```bash
curl -X POST http://localhost:8080/api/user/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "ticketId": 99999,
    "amount": 50000,
    "provider": "MOMO"
  }'
```

### 10.3 Không có quyền truy cập

```bash
curl http://localhost:8080/api/user/payments
# (không có Authorization header)
```

**Kết quả mong đợi:** HTTP 401 Unauthorized

---

## 🧹 Test 11: Cleanup

### Endpoint: GET /api/payment/test/cleanup

**Cách test:**

```bash
curl http://localhost:8080/api/payment/test/cleanup
```

**Kết quả mong đợi:**

```json
{
  "success": true,
  "message": "Cleanup completed successfully"
}
```

**Mục đích:** Dọn dẹp các thanh toán hết hạn

---

## 📊 Kiểm tra Database

### Kiểm tra bảng payments

```sql
SELECT id, transaction_id, status, amount, created_at
FROM payments
ORDER BY created_at DESC;
```

### Kiểm tra bảng payment_providers

```sql
SELECT * FROM payment_providers;
```

### Kiểm tra tickets được confirm

```sql
SELECT t.id, t.ticket_code, t.status, p.transaction_id
FROM tickets t
LEFT JOIN payments p ON t.id = p.ticket_id
WHERE p.status = 'COMPLETED';
```

---

## 🎯 Checklist Test

- [ ] ✅ Payment providers hoạt động
- [ ] ✅ Đăng ký user thành công
- [ ] ✅ Đăng nhập lấy được JWT token
- [ ] ✅ Tạo thanh toán MoMo thành công
- [ ] ✅ Tạo thanh toán SePay thành công
- [ ] ✅ Kiểm tra trạng thái thanh toán
- [ ] ✅ Lấy danh sách thanh toán
- [ ] ✅ Lấy chi tiết thanh toán
- [ ] ✅ Callback MoMo xử lý đúng
- [ ] ✅ Callback SePay xử lý đúng
- [ ] ✅ Các lỗi được xử lý đúng
- [ ] ✅ Cleanup hoạt động

---

## 🚨 Lưu ý quan trọng

1. **Thay thế YOUR_JWT_TOKEN** bằng token thật từ login
2. **Thay thế TRANSACTION_ID** bằng ID thật từ tạo thanh toán
3. **Cần có ticket trong database** để test thanh toán
4. **Kiểm tra logs** để debug nếu có lỗi
5. **MoMo và SePay** chỉ là test với sandbox/mock data

### 🔗 Về returnUrl:

**returnUrl là gì?**

- URL mà user sẽ được chuyển hướng về sau khi thanh toán xong
- Ví dụ: User quét QR MoMo → Thanh toán → Quay về `returnUrl`

**Có bắt buộc không?**

- ❌ **KHÔNG bắt buộc** - có thể bỏ qua khi test
- ✅ **Nên có** khi có frontend app (React/Vue)

**Ví dụ sử dụng:**

```bash
# Không cần returnUrl (test đơn giản)
{
  "ticketId": 1,
  "amount": 50000,
  "provider": "MOMO"
}

# Có returnUrl (khi có frontend)
{
  "ticketId": 1,
  "amount": 50000,
  "provider": "MOMO",
  "returnUrl": "http://localhost:3000/payment/success"
}
```

---

## 🎉 Kết luận

Khi tất cả tests pass, hệ thống thanh toán đã hoạt động đúng và sẵn sàng sử dụng!
