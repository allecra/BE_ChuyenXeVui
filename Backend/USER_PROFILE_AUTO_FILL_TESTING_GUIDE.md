# 👤 Hướng dẫn Test Auto-Fill Thông tin User Profile

## 📋 Tổng quan Tính năng

**Tính năng mới:** Tự động điền thông tin từ tài khoản user khi đặt vé, cho phép user chỉnh sửa nếu cần.

### Workflow:

1. **User đăng nhập** → Hệ thống có thông tin profile
2. **User chọn ghế** → Gọi API lấy thông tin profile để pre-fill form
3. **User đặt vé** → Nếu không gửi passengerInfo, tự động lấy từ profile
4. **User có thể override** → Gửi passengerInfo khác để ghi đè thông tin profile

## 🔧 Chuẩn bị Test

### 1. Database Migration

```sql
-- V18__Add_id_card_to_users.sql đã được tạo
-- Chạy migration để thêm trường id_card vào bảng users
```

### 2. Cập nhật User Profile

```bash
# Cập nhật thông tin user test để có đầy đủ thông tin
UPDATE users SET
    first_name = 'Nguyễn',
    last_name = 'Văn A',
    phone = '0123456789',
    id_card = '123456789012'
WHERE email = 'user@example.com';
```

### 3. Lấy JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

## 🎯 Test Cases - User Profile APIs

### Test 1: Lấy thông tin cá nhân

```bash
curl -X GET "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Lấy thông tin cá nhân thành công",
  "data": {
    "id": 1,
    "firstName": "Nguyễn",
    "lastName": "Văn A",
    "fullName": "Nguyễn Văn A",
    "email": "user@example.com",
    "phone": "0123456789",
    "idCard": "123456789012",
    "status": "ACTIVE"
  }
}
```

### Test 2: Lấy thông tin để điền form đặt vé

```bash
curl -X GET "http://localhost:8080/api/user/profile/for-booking" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:** Giống Test 1, nhưng dành riêng cho booking form

### Test 3: Cập nhật thông tin cá nhân

```bash
curl -X PUT "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Trần",
    "lastName": "Thị B",
    "email": "user@example.com",
    "phone": "0987654321",
    "idCard": "987654321098"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Cập nhật thông tin cá nhân thành công",
  "data": {
    "id": 1,
    "firstName": "Trần",
    "lastName": "Thị B",
    "fullName": "Trần Thị B",
    "email": "user@example.com",
    "phone": "0987654321",
    "idCard": "987654321098",
    "status": "ACTIVE"
  }
}
```

## 🎫 Test Cases - Auto-Fill Booking

### Test 4: Đặt vé KHÔNG gửi passengerInfo (Auto-fill từ profile)

```bash
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 1,
    "sessionId": "auto_fill_test_001"
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
    "passengerInfo": {
      "fullName": "Trần Thị B",
      "phoneNumber": "0987654321",
      "email": "user@example.com",
      "idCard": "987654321098"
    }
  }
}
```

**Verify Database:**

```sql
SELECT passenger_name, passenger_phone, passenger_email, passenger_id_card
FROM tickets WHERE id = 1;
-- Should match user profile data
```

### Test 5: Đặt vé CÓ gửi passengerInfo (Override profile)

```bash
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 2,
    "passengerInfo": {
      "fullName": "Người nhận vé khác",
      "phoneNumber": "0111222333",
      "email": "other@example.com",
      "idCard": "111222333444"
    },
    "sessionId": "override_test_001"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Đặt vé thành công",
  "data": {
    "ticketId": 2,
    "ticketCode": "TK20260317002",
    "passengerInfo": {
      "fullName": "Người nhận vé khác",
      "phoneNumber": "0111222333",
      "email": "other@example.com",
      "idCard": "111222333444"
    }
  }
}
```

### Test 6: Đặt vé với passengerInfo PARTIAL (Mix auto-fill + override)

```bash
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 3,
    "passengerInfo": {
      "fullName": "Tên khác",
      "email": "email-khac@example.com"
    },
    "sessionId": "partial_test_001"
  }'
```

**Expected Behavior:**

- fullName: "Tên khác" (từ request)
- phoneNumber: "0987654321" (từ profile)
- email: "email-khac@example.com" (từ request)
- idCard: "987654321098" (từ profile)

### Test 7: User chưa có thông tin đầy đủ trong profile

```bash
# Tạo user mới với thông tin không đầy đủ
INSERT INTO users (first_name, last_name, email, password, status)
VALUES ('Test', '', 'incomplete@example.com', '$2a$10$...', 'ACTIVE');

# Login với user này và test đặt vé
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer INCOMPLETE_USER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 4,
    "sessionId": "incomplete_test_001"
  }'
```

**Expected Behavior:**

- fullName: "Test" (chỉ có firstName)
- phoneNumber: null
- email: "incomplete@example.com" (từ user.email)
- idCard: null

## 🔄 Test Cases - Email Integration

### Test 8: Email với thông tin auto-fill

```bash
# Đặt vé với auto-fill và kiểm tra email
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 5,
    "sessionId": "email_test_001"
  }'
```

**Expected Email Content:**

- **To:** user@example.com (từ profile)
- **Passenger Name:** Trần Thị B (từ profile)
- **Phone:** 0987654321 (từ profile)
- **ID Card:** 987654321098 (từ profile)

### Test 9: Email với thông tin override

```bash
# Đặt vé với thông tin khác và kiểm tra email
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 6,
    "passengerInfo": {
      "fullName": "Người khác",
      "email": "nguoi-khac@example.com"
    },
    "sessionId": "email_override_test_001"
  }'
```

**Expected Email Content:**

- **To:** nguoi-khac@example.com (từ request)
- **Passenger Name:** Người khác (từ request)
- **Phone:** 0987654321 (từ profile - auto-fill)

## ⚠️ Test Cases - Error Scenarios

### Test 10: Cập nhật profile với email đã tồn tại

```bash
curl -X PUT "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "admin@example.com",
    "phone": "0123456789"
  }'
```

**Expected Response:**

```json
{
  "success": false,
  "message": "Email đã được sử dụng bởi tài khoản khác"
}
```

### Test 11: Cập nhật profile với CMND đã tồn tại

```bash
curl -X PUT "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "user@example.com",
    "phone": "0123456789",
    "idCard": "999888777666"
  }'
```

**Expected Response:**

```json
{
  "success": false,
  "message": "CMND/CCCD đã được sử dụng bởi tài khoản khác"
}
```

### Test 12: Validation errors

```bash
curl -X PUT "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "",
    "lastName": "User",
    "email": "invalid-email",
    "phone": "123",
    "idCard": "abc"
  }'
```

**Expected Response:**

```json
{
  "success": false,
  "message": "Validation errors",
  "errors": [
    "Tên không được để trống",
    "Email không hợp lệ",
    "Số điện thoại phải có 10-11 chữ số",
    "CMND/CCCD phải có 9-12 chữ số"
  ]
}
```

## 📊 Database Verification

### Kiểm tra User Profile

```sql
-- Xem thông tin user profile
SELECT id, first_name, last_name, email, phone, id_card, status
FROM users WHERE email = 'user@example.com';

-- Xem lịch sử cập nhật
SELECT id, first_name, last_name, email, phone, id_card, updated_at
FROM users WHERE email = 'user@example.com';
```

### Kiểm tra Ticket với Auto-fill

```sql
-- Xem thông tin passenger trong tickets
SELECT
    t.id,
    t.ticket_code,
    t.passenger_name,
    t.passenger_phone,
    t.passenger_email,
    t.passenger_id_card,
    u.first_name,
    u.last_name,
    u.email as user_email,
    u.phone as user_phone,
    u.id_card as user_id_card
FROM tickets t
JOIN users u ON t.user_id = u.id
WHERE t.user_id = 1
ORDER BY t.created_at DESC;
```

### Kiểm tra Email Logs

```sql
-- Nếu có bảng email_logs
SELECT ticket_id, email_type, recipient_email, sent_at, status
FROM email_logs
WHERE ticket_id IN (SELECT id FROM tickets WHERE user_id = 1)
ORDER BY sent_at DESC;
```

## 🔄 Complete Workflow Test

### Scenario 1: User mới cập nhật profile và đặt vé

```bash
# 1. User login
# 2. Cập nhật profile đầy đủ
curl -X PUT "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Lê",
    "lastName": "Văn C",
    "email": "user@example.com",
    "phone": "0999888777",
    "idCard": "123123123123"
  }'

# 3. Lấy thông tin để điền form
curl -X GET "http://localhost:8080/api/user/profile/for-booking" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 4. Đặt vé với auto-fill
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 7,
    "sessionId": "complete_workflow_001"
  }'

# 5. Kiểm tra email và database
```

### Scenario 2: User đặt vé cho người khác

```bash
# 1. Lấy thông tin profile (để hiển thị trong form)
curl -X GET "http://localhost:8080/api/user/profile/for-booking" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 2. User chỉnh sửa thông tin và đặt vé cho người khác
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 8,
    "passengerInfo": {
      "fullName": "Người thân",
      "phoneNumber": "0777666555",
      "email": "nguoi-than@example.com",
      "idCard": "555666777888"
    },
    "sessionId": "for_others_001"
  }'

# 3. Kiểm tra email được gửi đến nguoi-than@example.com
```

## 📝 Frontend Integration Notes

### API Endpoints mới:

1. **GET /api/user/profile** - Lấy thông tin cá nhân
2. **PUT /api/user/profile** - Cập nhật thông tin cá nhân
3. **GET /api/user/profile/for-booking** - Lấy thông tin để điền form đặt vé

### Frontend Workflow:

```javascript
// 1. Khi user vào trang đặt vé
const userProfile = await fetch("/api/user/profile/for-booking");

// 2. Pre-fill form với thông tin user
document.getElementById("fullName").value = userProfile.fullName;
document.getElementById("phone").value = userProfile.phone;
document.getElementById("email").value = userProfile.email;
document.getElementById("idCard").value = userProfile.idCard;

// 3. Cho phép user chỉnh sửa
// 4. Khi submit, chỉ gửi passengerInfo nếu user đã thay đổi
const bookingData = {
  scheduleId: 1,
  seatId: 2,
  sessionId: generateSessionId(),
};

// Chỉ thêm passengerInfo nếu user đã chỉnh sửa
if (hasUserModifiedInfo()) {
  bookingData.passengerInfo = {
    fullName: document.getElementById("fullName").value,
    phoneNumber: document.getElementById("phone").value,
    email: document.getElementById("email").value,
    idCard: document.getElementById("idCard").value,
  };
}

await fetch("/api/user/tickets/book", {
  method: "POST",
  body: JSON.stringify(bookingData),
});
```

## 🚀 Expected Results

### ✅ Auto-fill Functionality

- **Profile API** hoạt động đúng
- **Auto-fill** từ user profile khi không có passengerInfo
- **Override** thông tin khi có passengerInfo
- **Partial override** khi chỉ gửi một số trường

### ✅ User Experience

- **Form pre-filled** với thông tin user
- **Cho phép chỉnh sửa** mọi trường thông tin
- **Validation** đúng cho tất cả trường
- **Error handling** rõ ràng

### ✅ Email Integration

- **Email gửi đúng địa chỉ** (từ profile hoặc override)
- **Thông tin passenger** chính xác trong email
- **Template hiển thị** đúng thông tin

### ✅ Data Integrity

- **User profile** được lưu đúng
- **Ticket passenger info** chính xác
- **Không duplicate** email/idCard
- **Audit trail** đầy đủ

---

## 📞 Support

Nếu gặp vấn đề trong quá trình test:

1. **Check migration** đã chạy chưa: `SELECT * FROM flyway_schema_history WHERE version = '18';`
2. **Check user data**: `SELECT * FROM users WHERE email = 'user@example.com';`
3. **Check logs**: `tail -f logs/application.log | grep -E "(👤|📝|🎫)"`
4. **Verify JWT token** còn hạn và đúng user
5. **Check validation errors** trong response

**Happy Testing! 👤✨**
