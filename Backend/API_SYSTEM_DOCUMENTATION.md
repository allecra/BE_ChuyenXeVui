# 📚 Tài Liệu Hệ Thống API - CK_DatVeXe

> **Lưu ý quan trọng**: File này là tài liệu chính thức của hệ thống. Mọi thay đổi API phải được cập nhật vào đây với chi tiết đầy đủ.

---

## 🏗️ Kiến Trúc Hệ Thống

### 📊 Database Schema Overview

```
Users (người dùng)
├── Bus Companies (nhà xe)
├── Drivers (tài xế)
└── Tickets (vé đã đặt)

Routes (tuyến đường)
├── Route Stations (bến trong tuyến)
└── Schedules (lịch trình)
    ├── Schedule Buses (xe trong lịch trình)
    └── Tickets (vé của lịch trình)

Buses (xe buýt)
├── Seats (ghế ngồi)
├── Seat Locks (khóa ghế tạm thời)
└── Drivers (tài xế được gán)

Payments (thanh toán)
├── Payment Providers (nhà cung cấp)
└── Tickets (vé được thanh toán)

Discount Codes (mã giảm giá)
├── Discount Usage (lịch sử sử dụng)
└── Tickets (vé áp dụng giảm giá)

Reviews (đánh giá)
└── Users (người đánh giá)
```

### 🔐 Authentication & Authorization

#### JWT Token Structure

```json
{
  "sub": "user_id",
  "roles": ["USER", "COMPANY", "ADMIN"],
  "company_id": 123,
  "exp": 1640995200,
  "iat": 1640908800
}
```

#### Role Permissions Matrix

| Feature         | USER | COMPANY | ADMIN |
| --------------- | ---- | ------- | ----- |
| View Schedules  | ✅   | ✅      | ✅    |
| Book Tickets    | ✅   | ❌      | ✅    |
| Manage Buses    | ❌   | ✅      | ✅    |
| Manage Routes   | ❌   | ✅      | ✅    |
| Manage Users    | ❌   | ❌      | ✅    |
| System Settings | ❌   | ❌      | ✅    |

---

## 🚀 API Endpoints Documentation

### 1. 🔑 Authentication Module

#### Base URL: `/api/auth`

#### POST `/register` - Đăng ký tài khoản

**Request Body:**

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "phone": "0123456789",
  "role": "USER"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "phone": "0123456789",
      "roles": ["USER"],
      "status": "ACTIVE"
    }
  }
}
```

#### POST `/login` - Đăng nhập

**Request Body:**

```json
{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "phone": "0123456789",
      "roles": ["USER"],
      "status": "ACTIVE"
    }
  }
}
```

#### POST `/refresh-token` - Làm mới token

**Request Body:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**

```json
{
  "success": true,
  "message": "Token đã được làm mới",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

#### POST `/forgot-password` - Quên mật khẩu

**Request Body:**

```json
{
  "email": "john@example.com"
}
```

**Response:**

```json
{
  "success": true,
  "message": "OTP đã được gửi đến email của bạn",
  "data": {
    "email": "john@example.com",
    "otpExpiresAt": "2026-03-22T11:00:00"
  }
}
```

#### POST `/verify-otp` - Xác thực OTP

**Request Body:**

```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```

**Response:**

```json
{
  "success": true,
  "message": "OTP xác thực thành công",
  "data": {
    "email": "john@example.com",
    "resetToken": "temp_reset_token_123",
    "expiresAt": "2026-03-22T11:30:00"
  }
}
```

#### POST `/reset-password` - Đặt lại mật khẩu

**Request Body:**

```json
{
  "resetToken": "temp_reset_token_123",
  "newPassword": "NewSecurePass123!"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Mật khẩu đã được đặt lại thành công",
  "data": null
}
```

#### POST `/logout` - Đăng xuất

**Headers:** `Authorization: Bearer {accessToken}`
**Response:**

```json
{
  "success": true,
  "message": "Đăng xuất thành công",
  "data": null
}
```

#### Key Features:

- ✅ JWT Authentication với Access/Refresh Token
- ✅ OTP verification qua email
- ✅ Password reset workflow
- ✅ Role-based authorization
- ✅ Session management

---

### 2. 👤 User Management Module

#### User Profile API: `/api/users`

#### GET `/profile` - Xem thông tin cá nhân

**Headers:** `Authorization: Bearer {accessToken}`
**Response:**

```json
{
  "success": true,
  "message": "Lấy thông tin profile thành công",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "phone": "0123456789",
    "idCard": "123456789012",
    "status": "ACTIVE"
  }
}
```

#### PUT `/profile` - Cập nhật thông tin cá nhân

**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**

```json
{
  "fullName": "John Doe Updated",
  "phone": "0987654321",
  "idCard": "123456789012"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Cập nhật thông tin cá nhân thành công",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe Updated",
    "phone": "0987654321",
    "idCard": "123456789012",
    "status": "ACTIVE",
    "updatedAt": "2026-03-22T11:00:00"
  }
}
```

#### GET `/profile/for-booking` - Lấy thông tin để đặt vé

**Headers:** `Authorization: Bearer {accessToken}`
**Response:**

```json
{
  "success": true,
  "message": "Lấy thông tin để đặt vé thành công",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "phone": "0123456789",
    "idCard": "123456789012",
    "status": "ACTIVE"
  }
}
```

#### GET `/profile/booking-history` - Lịch sử đặt vé

**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**

- `page`: Số trang (default: 0)
- `size`: Kích thước trang (default: 10)
- `sortBy`: Sắp xếp theo (default: "createdAt")
- `sortDir`: Hướng sắp xếp (default: "desc")

**Response:**

```json
{
  "success": true,
  "message": "Lấy lịch sử đặt vé thành công",
  "data": {
    "content": [
      {
        "ticketId": 1,
        "ticketCode": "TK20260322001",
        "routeName": "Hà Nội - Hồ Chí Minh",
        "startStation": "Bến xe Miền Đông",
        "endStation": "Bến xe Miền Tây",
        "departureTime": "2026-03-23T08:00:00",
        "arrivalTime": "2026-03-23T20:00:00",
        "seatNumber": "A1",
        "seatType": "VIP",
        "price": 450000,
        "status": "CONFIRMED",
        "bookingTime": "2026-03-22T10:30:00",
        "busName": "Xe Limousine VIP",
        "licensePlate": "30A-12345",
        "companyName": "Nhà xe ABC",
        "paymentStatus": "COMPLETED",
        "paymentMethod": "MOMO"
      }
    ],
    "totalElements": 15,
    "totalPages": 2,
    "size": 10,
    "number": 0
  }
}
```

#### GET `/profile/payment-history` - Lịch sử thanh toán

**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**

- `page`: Số trang (default: 0)
- `size`: Kích thước trang (default: 10)
- `sortBy`: Sắp xếp theo (default: "createdAt")
- `sortDir`: Hướng sắp xếp (default: "desc")

**Response:**

```json
{
  "success": true,
  "message": "Lấy lịch sử thanh toán thành công",
  "data": {
    "content": [
      {
        "paymentId": 1,
        "transactionId": "PAY_20260322_001",
        "ticketCode": "TK20260322001",
        "routeName": "Hà Nội - Hồ Chí Minh",
        "amount": 450000,
        "currency": "VND",
        "paymentMethod": "MOMO",
        "status": "COMPLETED",
        "createdAt": "2026-03-22T10:30:00",
        "paidAt": "2026-03-22T10:32:00",
        "description": "Thanh toán vé xe khách TK20260322001",
        "providerName": "MoMo"
      }
    ],
    "totalElements": 8,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

#### GET `/profile/login-sessions` - Quản lý đăng nhập

**Headers:** `Authorization: Bearer {accessToken}`
**Response:**

```json
{
  "success": true,
  "message": "Lấy danh sách phiên đăng nhập thành công",
  "data": [
    {
      "sessionId": "sess_001",
      "deviceInfo": "Chrome on Windows",
      "ipAddress": "192.168.1.100",
      "location": "Hà Nội, Việt Nam",
      "loginTime": "2026-03-22T08:00:00",
      "lastActivity": "2026-03-22T10:25:00",
      "isActive": true,
      "browserInfo": "Chrome 120.0.0.0",
      "operatingSystem": "Windows 11"
    },
    {
      "sessionId": "sess_002",
      "deviceInfo": "Mobile App on Android",
      "ipAddress": "192.168.1.101",
      "location": "Hà Nội, Việt Nam",
      "loginTime": "2026-03-21T15:30:00",
      "lastActivity": "2026-03-22T09:15:00",
      "isActive": true,
      "browserInfo": "Mobile App",
      "operatingSystem": "Android 14"
    }
  ]
}
```

#### POST `/profile/change-password` - Đổi mật khẩu

**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**

```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewSecurePass123!",
  "confirmPassword": "NewSecurePass123!"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công. Email thông báo đã được gửi.",
  "data": null
}
```

#### Admin User Management: `/api/admin/users`

#### GET `/` - Danh sách người dùng

**Headers:** `Authorization: Bearer {accessToken}` (ADMIN role)
**Query Parameters:**

- `page`: Số trang (default: 0)
- `size`: Kích thước trang (default: 10)
- `sort`: Sắp xếp (default: "id")
- `direction`: Hướng sắp xếp (default: "asc")
- `search`: Tìm kiếm theo tên/email
- `status`: Lọc theo trạng thái

**Response:**

```json
{
  "success": true,
  "message": "Lấy danh sách người dùng thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "john_doe",
        "email": "john@example.com",
        "fullName": "John Doe",
        "phone": "0123456789",
        "idCard": "123456789012",
        "status": "ACTIVE",
        "createdAt": "2026-03-22T10:00:00",
        "updatedAt": "2026-03-22T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

#### GET `/{id}` - Chi tiết người dùng

**Headers:** `Authorization: Bearer {accessToken}` (ADMIN role)
**Response:**

```json
{
  "success": true,
  "message": "Lấy thông tin người dùng thành công",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "phone": "0123456789",
    "idCard": "123456789012",
    "status": "ACTIVE",
    "roles": ["USER"],
    "createdAt": "2026-03-22T10:00:00",
    "updatedAt": "2026-03-22T10:00:00",
    "busCompany": null
  }
}
```

#### PUT `/{id}` - Cập nhật người dùng

**Headers:** `Authorization: Bearer {accessToken}` (ADMIN role)
**Request Body:**

```json
{
  "fullName": "John Doe Updated",
  "phone": "0987654321",
  "idCard": "123456789012",
  "status": "ACTIVE"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Cập nhật người dùng thành công",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe Updated",
    "phone": "0987654321",
    "idCard": "123456789012",
    "status": "ACTIVE",
    "updatedAt": "2026-03-22T11:00:00"
  }
}
```

#### DELETE `/{id}` - Xóa người dùng (soft delete)

**Headers:** `Authorization: Bearer {accessToken}` (ADMIN role)
**Request Body:**

```json
{
  "reason": "Vi phạm chính sách sử dụng"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Xóa người dùng thành công",
  "data": {
    "deletedUserId": 1,
    "deletedAt": "2026-03-22T11:00:00",
    "reason": "Vi phạm chính sách sử dụng",
    "deletedBy": "admin_user"
  }
}
```

#### Key Features:

- ✅ Auto-fill profile information trong booking
- ✅ Soft delete với audit trail
- ✅ User status management (Active, Blocked, Deleted)
- ✅ Admin user management với full CRUD

---

### 3. 🎫 Ticket Booking Module

#### User Ticket API: `/api/user/tickets`

#### POST `/book` - Đặt vé

**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**

```json
{
  "scheduleId": 1,
  "seatNumbers": ["A1", "A2"],
  "passengerInfo": {
    "fullName": "John Doe",
    "phone": "0123456789",
    "email": "john@example.com",
    "idCard": "123456789012"
  },
  "discountCode": "SUMMER2026",
  "notes": "Yêu cầu đặc biệt"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Đặt vé thành công",
  "data": {
    "ticketId": 1,
    "ticketCode": "TK20260322001",
    "scheduleId": 1,
    "seatNumber": "A1",
    "seatType": "VIP",
    "price": 450000,
    "originalPrice": 500000,
    "discountAmount": 50000,
    "status": "PENDING",
    "paymentDeadline": "2026-03-22T10:35:00",
    "createdAt": "2026-03-22T10:30:00",
    "passengerInfo": {
      "fullName": "John Doe",
      "phone": "0123456789",
      "email": "john@example.com",
      "idCard": "123456789012"
    },
    "routeInfo": {
      "routeName": "Hà Nội - Hồ Chí Minh",
      "startStation": "Bến xe Miền Đông",
      "endStation": "Bến xe Miền Tây",
      "departureTime": "2026-03-23T08:00:00",
      "arrivalTime": "2026-03-23T20:00:00"
    }
  }
}
```

#### GET `/` - Danh sách vé của tôi

**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**

- `page`: Số trang (default: 0)
- `size`: Kích thước trang (default: 10)
- `status`: Lọc theo trạng thái vé
- `fromDate`: Từ ngày
- `toDate`: Đến ngày

**Response:**

```json
{
  "success": true,
  "message": "Lấy danh sách vé thành công",
  "data": {
    "content": [
      {
        "ticketId": 1,
        "ticketCode": "TK20260322001",
        "scheduleId": 1,
        "seatNumber": "A1",
        "seatType": "VIP",
        "price": 450000,
        "status": "CONFIRMED",
        "departureTime": "2026-03-23T08:00:00",
        "routeName": "Hà Nội - Hồ Chí Minh",
        "startStation": "Bến xe Miền Đông",
        "endStation": "Bến xe Miền Tây",
        "createdAt": "2026-03-22T10:30:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

#### GET `/{id}` - Chi tiết vé

**Headers:** `Authorization: Bearer {accessToken}`
**Response:**

```json
{
  "success": true,
  "message": "Lấy thông tin vé thành công",
  "data": {
    "ticketId": 1,
    "ticketCode": "TK20260322001",
    "scheduleId": 1,
    "seatNumber": "A1",
    "seatType": "VIP",
    "price": 450000,
    "originalPrice": 500000,
    "discountAmount": 50000,
    "status": "CONFIRMED",
    "paymentDeadline": "2026-03-22T10:35:00",
    "createdAt": "2026-03-22T10:30:00",
    "passengerInfo": {
      "fullName": "John Doe",
      "phone": "0123456789",
      "email": "john@example.com",
      "idCard": "123456789012"
    },
    "routeInfo": {
      "routeName": "Hà Nội - Hồ Chí Minh",
      "startStation": "Bến xe Miền Đông",
      "endStation": "Bến xe Miền Tây",
      "departureTime": "2026-03-23T08:00:00",
      "arrivalTime": "2026-03-23T20:00:00"
    },
    "busInfo": {
      "busName": "Xe Limousine VIP",
      "licensePlate": "30A-12345",
      "busType": "LIMOUSINE"
    },
    "paymentInfo": {
      "paymentId": 1,
      "paymentMethod": "MOMO",
      "paymentStatus": "COMPLETED",
      "paidAt": "2026-03-22T10:32:00"
    }
  }
}
```

#### POST `/{id}/cancel` - Hủy vé

**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**

```json
{
  "cancellationReason": "Thay đổi kế hoạch đi lại",
  "bankAccountNumber": "1234567890",
  "bankName": "Vietcombank",
  "bankAccountName": "John Doe"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Hủy vé thành công",
  "data": {
    "ticketId": 1,
    "ticketCode": "TK20260322001",
    "originalAmount": 450000,
    "refundAmount": 360000,
    "cancellationFee": 90000,
    "refundPercentage": 80,
    "cancellationReason": "Thay đổi kế hoạch đi lại",
    "cancellationTime": "2026-03-22T11:00:00",
    "estimatedRefundTime": "2026-03-29T11:00:00",
    "refundMethod": "Chuyển khoản ngân hàng",
    "refundStatus": "Đang xử lý",
    "message": "Hủy vé thành công. Tiền hoàn sẽ được chuyển trong vòng 7 ngày làm việc."
  }
}
```

#### Key Features:

- ✅ **Seat Locking**: Khóa ghế 5 phút khi đặt vé
- ✅ **Auto-fill Profile**: Tự động điền thông tin từ profile
- ✅ **Email Notifications**: Thông báo tất cả trạng thái vé
- ✅ **Cancellation Policy**: Chính sách hủy vé linh hoạt
- ✅ **Ticket Modification**: Đổi vé sang lịch trình khác
- ✅ **Discount Integration**: Tích hợp mã giảm giá

---

### 4. 💳 Payment System Module

#### Public Payment API: `/api/payments`

#### POST `/create` - Tạo thanh toán

**Request Body:**

```json
{
  "ticketId": 1,
  "paymentMethod": "MOMO",
  "amount": 450000,
  "currency": "VND",
  "description": "Thanh toán vé xe khách TK20260322001",
  "returnUrl": "https://app.datveexe.com/payment/success",
  "cancelUrl": "https://app.datveexe.com/payment/cancel"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Tạo thanh toán thành công",
  "data": {
    "paymentId": 1,
    "transactionId": "PAY_20260322_001",
    "provider": "MOMO",
    "amount": 450000,
    "currency": "VND",
    "status": "PENDING",
    "qrCodeUrl": "https://api.momo.vn/qr/123456",
    "paymentUrl": "https://payment.momo.vn/pay/123456",
    "expiredAt": "2026-03-22T10:45:00",
    "createdAt": "2026-03-22T10:30:00"
  }
}
```

#### POST `/callback` - Callback từ payment gateway

**Request Body (MoMo):**

```json
{
  "partnerCode": "MOMO",
  "orderId": "PAY_20260322_001",
  "requestId": "REQ_20260322_001",
  "amount": 450000,
  "orderInfo": "Thanh toán vé xe khách",
  "orderType": "momo_wallet",
  "transId": "2260322001",
  "resultCode": 0,
  "message": "Successful.",
  "payType": "qr",
  "responseTime": 1640995200000,
  "extraData": "",
  "signature": "signature_hash"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Xử lý callback thành công",
  "data": {
    "paymentId": 1,
    "status": "COMPLETED",
    "processedAt": "2026-03-22T10:32:00"
  }
}
```

#### GET `/{id}/status` - Kiểm tra trạng thái thanh toán

**Response:**

```json
{
  "success": true,
  "message": "Lấy trạng thái thanh toán thành công",
  "data": {
    "paymentId": 1,
    "transactionId": "PAY_20260322_001",
    "provider": "MOMO",
    "amount": 450000,
    "currency": "VND",
    "status": "COMPLETED",
    "paidAt": "2026-03-22T10:32:00",
    "ticketId": 1,
    "userId": 1
  }
}
```

#### Key Features:

- ✅ **Multi-Gateway Support**: MoMo, SePay integration
- ✅ **QR Code Payment**: Tạo QR code cho thanh toán
- ✅ **Callback Handling**: Xử lý webhook từ payment gateway
- ✅ **Auto Refund**: Hoàn tiền tự động khi hủy vé
- ✅ **Payment Tracking**: Theo dõi trạng thái thanh toán
- ✅ **Expired Payment Cleanup**: Tự động dọn dẹp thanh toán hết hạn

---

### 5. 🎁 Discount System Module

#### User Discount API: `/api/user/discounts`

#### GET `/available` - Mã giảm giá khả dụng

**Headers:** `Authorization: Bearer {accessToken}`
**Response:**

```json
{
  "success": true,
  "message": "Lấy danh sách mã giảm giá thành công",
  "data": [
    {
      "id": 1,
      "code": "SUMMER2026",
      "name": "Khuyến mãi mùa hè 2026",
      "description": "Giảm 10% cho tất cả chuyến xe",
      "discountType": "PERCENTAGE",
      "discountValue": 10,
      "maxDiscountAmount": 100000,
      "minOrderAmount": 200000,
      "validFrom": "2026-06-01T00:00:00",
      "validTo": "2026-08-31T23:59:59",
      "usageLimit": 1000,
      "usedCount": 150,
      "usageLimitPerUser": 3,
      "status": "ACTIVE",
      "scope": "PLATFORM"
    }
  ]
}
```

#### POST `/validate` - Kiểm tra mã giảm giá

**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**

```json
{
  "discountCode": "SUMMER2026",
  "orderAmount": 500000,
  "scheduleId": 1
}
```

**Response:**

```json
{
  "success": true,
  "message": "Mã giảm giá hợp lệ",
  "data": {
    "isValid": true,
    "discountCode": "SUMMER2026",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "discountAmount": 50000,
    "finalAmount": 450000,
    "message": "Áp dụng mã giảm giá thành công"
  }
}
```

#### POST `/apply` - Áp dụng mã giảm giá

**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**

```json
{
  "discountCode": "SUMMER2026",
  "orderAmount": 500000,
  "scheduleId": 1
}
```

**Response:**

```json
{
  "success": true,
  "message": "Áp dụng mã giảm giá thành công",
  "data": {
    "discountUsageId": 1,
    "discountCode": "SUMMER2026",
    "originalAmount": 500000,
    "discountAmount": 50000,
    "finalAmount": 450000,
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "appliedAt": "2026-03-22T10:30:00"
  }
}
```

#### Company Discount API: `/api/company/discounts`

#### POST `/` - Tạo mã giảm giá

**Headers:** `Authorization: Bearer {accessToken}` (COMPANY role)
**Request Body:**

```json
{
  "code": "COMPANY2026",
  "name": "Khuyến mãi công ty",
  "description": "Giảm giá đặc biệt cho khách hàng thân thiết",
  "discountType": "FIXED_AMOUNT",
  "discountValue": 50000,
  "maxDiscountAmount": 50000,
  "minOrderAmount": 300000,
  "validFrom": "2026-03-22T00:00:00",
  "validTo": "2026-12-31T23:59:59",
  "usageLimit": 500,
  "usageLimitPerUser": 2,
  "scope": "COMPANY",
  "terms": "Áp dụng cho tất cả tuyến đường của công ty"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Tạo mã giảm giá thành công",
  "data": {
    "id": 2,
    "code": "COMPANY2026",
    "name": "Khuyến mãi công ty",
    "description": "Giảm giá đặc biệt cho khách hàng thân thiết",
    "discountType": "FIXED_AMOUNT",
    "discountValue": 50000,
    "maxDiscountAmount": 50000,
    "minOrderAmount": 300000,
    "validFrom": "2026-03-22T00:00:00",
    "validTo": "2026-12-31T23:59:59",
    "usageLimit": 500,
    "usedCount": 0,
    "usageLimitPerUser": 2,
    "status": "ACTIVE",
    "scope": "COMPANY",
    "createdAt": "2026-03-22T10:30:00"
  }
}
```

#### Key Features:

- ✅ **Flexible Discount Types**: Percentage, Fixed amount, Free shipping
- ✅ **Scope Management**: Platform-wide hoặc Company-specific
- ✅ **Usage Limits**: Giới hạn số lần sử dụng per user/total
- ✅ **Time-based**: Thời gian hiệu lực của mã
- ✅ **Minimum Order**: Điều kiện đơn hàng tối thiểu
- ✅ **Integration**: Tích hợp trực tiếp vào booking flow

---

## 🔧 Tính Năng Đặc Biệt

### 1. 🎯 Auto-Fill Profile System

```java
// Tự động điền thông tin hành khách từ user profile
BookTicketRequest request = new BookTicketRequest();
// Nếu passengerInfo null, tự động lấy từ user profile
if (request.getPassengerInfo() == null) {
    request.setPassengerInfo(userService.getProfileAsPassengerInfo(userId));
}
```

### 2. 🔒 Seat Locking Mechanism

```java
// Khóa ghế trong 5 phút khi đặt vé
SeatLock seatLock = new SeatLock();
seatLock.setExpiresAt(LocalDateTime.now().plusMinutes(5));
seatLock.setStatus(LockStatus.ACTIVE);
```

### 3. 📧 Email Notification System

- **Booking Confirmation**: Xác nhận đặt vé
- **Payment Success**: Thanh toán thành công
- **Payment Reminder**: Nhắc nhở thanh toán
- **Cancellation**: Thông báo hủy vé
- **Expiration**: Vé hết hạn

### 4. 💰 Segment-Based Pricing

```java
// Tính giá vé theo quãng đường
public Double calculatePrice(Integer startStationId, Integer endStationId, Integer routeId) {
    List<RouteStation> stations = routeStationService.getStationsBetween(
        routeId, startStationId, endStationId);
    return stations.stream()
        .mapToDouble(RouteStation::getPrice)
        .sum();
}
```

### 5. 🚌 Multi-Bus Schedule Support

```java
// Một lịch trình có thể có nhiều xe
@Entity
public class Schedule {
    @OneToMany(mappedBy = "schedule")
    private List<ScheduleBus> scheduleBuses;

    // Legacy single bus support
    @ManyToOne
    private Bus bus;
}
```

---

## 📊 Database Migrations

### Current Version: V20

| Version | Description                       | Date       | Status     |
| ------- | --------------------------------- | ---------- | ---------- |
| V10     | Fix bus_station table             | 2026-02-04 | ✅ Applied |
| V11     | Update routes table               | 2026-02-05 | ✅ Applied |
| V12     | Create route_stations table       | 2026-02-06 | ✅ Applied |
| V13     | Update schedules table            | 2026-02-07 | ✅ Applied |
| V14     | Create schedule_buses table       | 2026-02-08 | ✅ Applied |
| V15     | Create enhanced payments table    | 2026-02-09 | ✅ Applied |
| V16     | Create seat_locks table           | 2026-02-10 | ✅ Applied |
| V17     | Update seat and ticket status     | 2026-02-11 | ✅ Applied |
| V18     | Add id_card to users              | 2026-02-12 | ✅ Applied |
| V19     | Create deleted_users table        | 2026-02-13 | ✅ Applied |
| V20     | Create discount and review tables | 2026-03-22 | ✅ Applied |

---

## 🚨 Breaking Changes Log

### Version 1.0.0 (Current)

- **PaymentProvider**: Chuyển từ enum sang entity
- **Multi-Bus Schedule**: Thêm ScheduleBus entity
- **Seat Locking**: Thêm SeatLock mechanism
- **Auto-Fill Profile**: Thay đổi BookTicketRequest structure

---

_Tài liệu này được cập nhật lần cuối: 22/03/2026_
_Phiên bản API hiện tại: 1.0.0_
