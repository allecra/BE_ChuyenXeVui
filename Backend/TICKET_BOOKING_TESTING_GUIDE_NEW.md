# 🎫 Hướng dẫn Test API Đặt vé Xe khách (Updated Flow)

## 📋 Tổng quan Workflow Mới

**Workflow chính xác:**

1. **User chọn ghế** → **Đặt vé (tạo booking + lock ghế)** → **Nhập thông tin** → **Thanh toán**
2. **Thanh toán thành công** → Ghế BOOKED, Vé CONFIRMED
3. **Thanh toán thất bại/Hủy** → Ghế AVAILABLE, Vé CANCELLED/EXPIRED (sau 5 phút)

**Thời gian:** 5 phút để hoàn tất thanh toán (giảm từ 10 phút)

## 🔧 Chuẩn bị Test

### 1. Tạo dữ liệu test

```bash
# Đảm bảo có:
# - User với role USER
# - Company với role COMPANY
# - Schedule với bus đã gán ghế (status AVAILABLE)
```

### 2. Lấy JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

## 🎯 Test Cases - NEW WORKFLOW

### Test 1: Xem sơ đồ ghế của chuyến

```bash
curl -X GET "http://localhost:8080/api/user/schedules/1/seats" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Lấy sơ đồ ghế thành công",
  "data": {
    "scheduleId": 1,
    "busInfo": {
      "busNumber": "29B-12345",
      "busType": "LIMOUSINE",
      "totalSeats": 40
    },
    "seats": [
      {
        "seatId": 1,
        "seatNumber": "A1",
        "seatType": "VIP",
        "price": 350000,
        "status": "AVAILABLE",
        "position": { "row": 1, "column": 1 }
      }
    ]
  }
}
```

### Test 2: 🆕 Đặt vé (Tạo booking + Lock ghế ngay lập tức)

```bash
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 1,
    "passengerInfo": {
      "fullName": "Nguyễn Văn A",
      "phoneNumber": "0123456789",
      "email": "nguyenvana@example.com",
      "idCard": "123456789"
    },
    "sessionId": "user_session_123"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Đặt vé thành công",
  "data": {
    "ticketId": 1,
    "ticketCode": "TK20260317001",
    "scheduleId": 1,
    "seatNumber": "A1",
    "seatType": "VIP",
    "price": 350000,
    "status": "PENDING",
    "createdAt": "2026-03-17T10:00:00",
    "paymentDeadline": "2026-03-17T10:05:00",
    "remainingSeconds": 300,
    "paymentRequired": true,
    "passengerInfo": {
      "fullName": "Nguyễn Văn A",
      "phoneNumber": "0123456789",
      "email": "nguyenvana@example.com",
      "idCard": "123456789"
    }
  }
}
```

### Test 3: Kiểm tra ghế đã bị lock

```bash
curl -X GET "http://localhost:8080/api/user/schedules/1/seats" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected:** Ghế A1 sẽ có status = "LOCKED"

### Test 4: User khác thử đặt cùng ghế (Should fail)

```bash
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer ANOTHER_USER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 1,
    "passengerInfo": {
      "fullName": "Trần Thị B"
    }
  }'
```

**Expected Response:**

```json
{
  "success": false,
  "message": "Ghế đang được giữ bởi người khác"
}
```

### Test 5: Thanh toán thành công (Simulate)

```bash
# Gọi API thanh toán (sẽ tích hợp với Payment system)
# Sau khi thanh toán thành công, hệ thống sẽ gọi:
# ticketService.confirmTicketPayment(ticketId)

# Kiểm tra vé sau thanh toán:
curl -X GET "http://localhost:8080/api/user/tickets/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected:** status = "CONFIRMED", ghế status = "BOOKED"

### Test 6: Hủy vé đang chờ thanh toán

```bash
curl -X POST "http://localhost:8080/api/user/tickets/1/cancel" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Hủy vé chờ thanh toán thành công",
  "data": {
    "ticketId": 1,
    "status": "CANCELLED"
  }
}
```

**Expected:** Ghế trở về status = "AVAILABLE"

### Test 7: Để vé hết hạn tự động (5 phút)

```bash
# 1. Đặt vé
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 2,
    "passengerInfo": {
      "fullName": "Test User"
    }
  }'

# 2. Đợi 5 phút hoặc chạy scheduled task thủ công
# 3. Kiểm tra vé và ghế
curl -X GET "http://localhost:8080/api/user/tickets/2" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected:** Vé status = "EXPIRED", ghế status = "AVAILABLE"

## 🏢 Test Cases - COMPANY APIs (Không thay đổi)

### Test 8: Company đặt vé hộ khách (Bỏ qua lock)

```bash
curl -X POST "http://localhost:8080/api/company/tickets/book-for-customer" \
  -H "Authorization: Bearer COMPANY_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 3,
    "customerInfo": {
      "fullName": "Trần Thị B",
      "phoneNumber": "0987654321",
      "email": "tranthib@example.com"
    },
    "paymentMethod": "CASH",
    "notes": "Khách đặt tại quầy"
  }'
```

**Expected:** Vé CONFIRMED ngay lập tức, không cần chờ thanh toán

## ⚠️ Test Cases - Error Scenarios

### Test 9: Đặt vé khi đã có vé PENDING

```bash
# User đã có 1 vé PENDING, thử đặt thêm vé khác
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 4,
    "passengerInfo": {
      "fullName": "Test User 2"
    }
  }'
```

**Expected:** Thành công (user có thể đặt nhiều vé cùng lúc)

### Test 10: Thanh toán vé đã hết hạn

```bash
# Thử confirm payment cho vé đã EXPIRED
# ticketService.confirmTicketPayment(expiredTicketId)
```

**Expected:** Error "Vé không ở trạng thái chờ thanh toán"

## 🔄 Complete Workflow Tests

### Scenario 1: Đặt vé thành công

```bash
# 1. Xem ghế → 2. Đặt vé → 3. Thanh toán → 4. Vé CONFIRMED
```

### Scenario 2: Đặt vé rồi hủy

```bash
# 1. Đặt vé → 2. Hủy vé → 3. Ghế AVAILABLE
```

### Scenario 3: Đặt vé rồi để hết hạn

```bash
# 1. Đặt vé → 2. Đợi 5 phút → 3. Vé EXPIRED, ghế AVAILABLE
```

### Scenario 4: Concurrent booking

```bash
# 2 user cùng đặt 1 ghế → User đầu thành công, user sau fail
```

## 📊 Database Verification

### Kiểm tra sau khi đặt vé

```sql
-- Vé PENDING
SELECT * FROM tickets WHERE status = 'PENDING';

-- Ghế LOCKED
SELECT * FROM seats WHERE status = 'LOCKED';

-- Lock ACTIVE
SELECT * FROM seat_locks WHERE status = 'ACTIVE';
```

### Kiểm tra sau thanh toán thành công

```sql
-- Vé CONFIRMED
SELECT * FROM tickets WHERE status = 'CONFIRMED';

-- Ghế BOOKED
SELECT * FROM seats WHERE status = 'BOOKED';

-- Lock CONVERTED
SELECT * FROM seat_locks WHERE status = 'CONVERTED';
```

### Kiểm tra sau hết hạn

```sql
-- Vé EXPIRED
SELECT * FROM tickets WHERE status = 'EXPIRED';

-- Ghế AVAILABLE
SELECT * FROM seats WHERE status = 'AVAILABLE';

-- Lock EXPIRED
SELECT * FROM seat_locks WHERE status = 'EXPIRED';
```

## 🕐 Scheduled Tasks

### Test cleanup (chạy mỗi phút)

```bash
# Tạo vé PENDING và đợi 5+ phút
# Kiểm tra log: "🔒 [SCHEDULED] Starting cleanup of expired seat locks"
# Verify: Vé EXPIRED, ghế AVAILABLE, lock EXPIRED
```

## 📝 Key Changes từ workflow cũ

1. **Không còn API lock ghế riêng** - Lock được tạo khi đặt vé
2. **Thời gian giảm từ 10 → 5 phút** cho thanh toán
3. **Tạo vé ngay khi chọn ghế** thay vì tạo từ lock
4. **API đặt vé mới**: `POST /api/user/tickets/book`
5. **Hủy vé PENDING** được xử lý riêng
6. **Auto-expire** cả vé và lock sau 5 phút

## 🚀 Expected Results

- ✅ User đặt vé → Ghế lock ngay → 5 phút để thanh toán
- ✅ Thanh toán thành công → Vé CONFIRMED, ghế BOOKED
- ✅ Hủy/Hết hạn → Vé CANCELLED/EXPIRED, ghế AVAILABLE
- ✅ Không double booking cùng ghế
- ✅ Company đặt vé hộ → CONFIRMED ngay lập tức
- ✅ Cleanup tự động sau 5 phút
