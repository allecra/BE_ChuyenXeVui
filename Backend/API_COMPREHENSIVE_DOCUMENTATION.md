# 📚 Tài Liệu API Tổng Hợp - Hệ Thống Đặt Vé Xe Khách

## 🎯 Tổng Quan Hệ Thống

Hệ thống đặt vé xe khách với các module chính:

- **Authentication & Authorization**: Xác thực và phân quyền người dùng
- **Route Management**: Quản lý tuyến đường và bến xe
- **Schedule Management**: Quản lý lịch trình xe
- **Bus Management**: Quản lý xe và ghế ngồi
- **Ticket Booking**: Đặt vé và quản lý vé
- **Payment System**: Hệ thống thanh toán
- **Discount System**: Hệ thống mã giảm giá
- **Review System**: Hệ thống đánh giá
- **Driver Management**: Quản lý tài xế
- **User Management**: Quản lý người dùng

## 🔐 Phân Quyền Hệ Thống

### Vai Trò (Roles)

- **USER**: Khách hàng đặt vé
- **COMPANY**: Nhà xe quản lý lịch trình và xe
- **ADMIN**: Quản trị viên hệ thống

### Quyền Truy Cập

- **USER**: Xem lịch trình, đặt vé, thanh toán, đánh giá
- **COMPANY**: Quản lý xe, lịch trình, tài xế, xem báo cáo
- **ADMIN**: Quản lý toàn bộ hệ thống, người dùng, nhà xe

---

## 🚀 API Endpoints Theo Module

### 1. 🔑 Authentication API

#### Base URL: `/api/auth`

| Method | Endpoint           | Description       | Role          |
| ------ | ------------------ | ----------------- | ------------- |
| POST   | `/register`        | Đăng ký tài khoản | Public        |
| POST   | `/login`           | Đăng nhập         | Public        |
| POST   | `/refresh-token`   | Làm mới token     | Public        |
| POST   | `/forgot-password` | Quên mật khẩu     | Public        |
| POST   | `/verify-otp`      | Xác thực OTP      | Public        |
| POST   | `/reset-password`  | Đặt lại mật khẩu  | Public        |
| POST   | `/logout`          | Đăng xuất         | Authenticated |

### 2. 👤 User Management API

#### Base URL: `/api/users`

| Method | Endpoint   | Description                | Role |
| ------ | ---------- | -------------------------- | ---- |
| GET    | `/profile` | Xem thông tin cá nhân      | USER |
| PUT    | `/profile` | Cập nhật thông tin cá nhân | USER |

#### Admin User Management: `/api/admin/users`

| Method | Endpoint        | Description                  | Role  |
| ------ | --------------- | ---------------------------- | ----- |
| GET    | `/`             | Danh sách người dùng         | ADMIN |
| GET    | `/{id}`         | Chi tiết người dùng          | ADMIN |
| PUT    | `/{id}`         | Cập nhật người dùng          | ADMIN |
| DELETE | `/{id}`         | Xóa người dùng (soft delete) | ADMIN |
| POST   | `/{id}/block`   | Khóa tài khoản               | ADMIN |
| POST   | `/{id}/unblock` | Mở khóa tài khoản            | ADMIN |
| GET    | `/deleted`      | Danh sách người dùng đã xóa  | ADMIN |
| POST   | `/{id}/restore` | Khôi phục người dùng         | ADMIN |

### 3. 🚌 Bus Management API

#### User Bus API: `/api/user/buses`

| Method | Endpoint  | Description              | Role |
| ------ | --------- | ------------------------ | ---- |
| GET    | `/`       | Danh sách xe (công khai) | USER |
| GET    | `/{id}`   | Chi tiết xe              | USER |
| GET    | `/search` | Tìm kiếm xe              | USER |

#### Company Bus API: `/api/company/buses`

| Method | Endpoint               | Description              | Role    |
| ------ | ---------------------- | ------------------------ | ------- |
| GET    | `/`                    | Danh sách xe của công ty | COMPANY |
| POST   | `/`                    | Thêm xe mới              | COMPANY |
| GET    | `/{id}`                | Chi tiết xe              | COMPANY |
| PUT    | `/{id}`                | Cập nhật thông tin xe    | COMPANY |
| DELETE | `/{id}`                | Xóa xe                   | COMPANY |
| POST   | `/{id}/seats/generate` | Tạo sơ đồ ghế            | COMPANY |

#### Admin Bus API: `/api/admin/buses`

| Method | Endpoint       | Description            | Role  |
| ------ | -------------- | ---------------------- | ----- |
| GET    | `/`            | Danh sách tất cả xe    | ADMIN |
| GET    | `/{id}`        | Chi tiết xe            | ADMIN |
| PUT    | `/{id}/status` | Cập nhật trạng thái xe | ADMIN |

### 4. 🛣️ Route Management API

#### User Route API: `/api/user/routes`

| Method | Endpoint                  | Description                  | Role |
| ------ | ------------------------- | ---------------------------- | ---- |
| GET    | `/`                       | Danh sách tuyến đường        | USER |
| GET    | `/{id}`                   | Chi tiết tuyến đường         | USER |
| GET    | `/search`                 | Tìm kiếm tuyến đường         | USER |
| GET    | `/{id}/stations`          | Danh sách bến trong tuyến    | USER |
| GET    | `/{id}/price-calculation` | Tính giá vé theo quãng đường | USER |

#### Company Route API: `/api/company/routes`

| Method | Endpoint                     | Description                 | Role    |
| ------ | ---------------------------- | --------------------------- | ------- |
| GET    | `/`                          | Danh sách tuyến của công ty | COMPANY |
| POST   | `/`                          | Tạo tuyến đường mới         | COMPANY |
| GET    | `/{id}`                      | Chi tiết tuyến đường        | COMPANY |
| PUT    | `/{id}`                      | Cập nhật tuyến đường        | COMPANY |
| DELETE | `/{id}`                      | Xóa tuyến đường             | COMPANY |
| POST   | `/{id}/stations`             | Thêm bến vào tuyến          | COMPANY |
| PUT    | `/{id}/stations/{stationId}` | Cập nhật thông tin bến      | COMPANY |
| DELETE | `/{id}/stations/{stationId}` | Xóa bến khỏi tuyến          | COMPANY |

### 5. 📅 Schedule Management API

#### User Schedule API: `/api/user/schedules`

| Method | Endpoint      | Description          | Role |
| ------ | ------------- | -------------------- | ---- |
| GET    | `/`           | Danh sách lịch trình | USER |
| GET    | `/{id}`       | Chi tiết lịch trình  | USER |
| GET    | `/search`     | Tìm kiếm lịch trình  | USER |
| GET    | `/{id}/seats` | Xem sơ đồ ghế        | USER |

#### Company Schedule API: `/api/company/schedules`

| Method | Endpoint              | Description                  | Role    |
| ------ | --------------------- | ---------------------------- | ------- |
| GET    | `/`                   | Danh sách lịch trình công ty | COMPANY |
| POST   | `/`                   | Tạo lịch trình mới           | COMPANY |
| GET    | `/{id}`               | Chi tiết lịch trình          | COMPANY |
| PUT    | `/{id}`               | Cập nhật lịch trình          | COMPANY |
| DELETE | `/{id}`               | Hủy lịch trình               | COMPANY |
| POST   | `/{id}/buses`         | Gán xe vào lịch trình        | COMPANY |
| PUT    | `/{id}/buses/{busId}` | Cập nhật trạng thái xe       | COMPANY |
| DELETE | `/{id}/buses/{busId}` | Gỡ xe khỏi lịch trình        | COMPANY |

### 6. 🎫 Ticket Management API

#### User Ticket API: `/api/user/tickets`

| Method | Endpoint       | Description          | Role |
| ------ | -------------- | -------------------- | ---- |
| POST   | `/book`        | Đặt vé               | USER |
| GET    | `/`            | Danh sách vé của tôi | USER |
| GET    | `/{id}`        | Chi tiết vé          | USER |
| POST   | `/{id}/cancel` | Hủy vé               | USER |
| POST   | `/{id}/modify` | Đổi vé               | USER |

#### Company Ticket API: `/api/company/tickets`

| Method | Endpoint       | Description              | Role    |
| ------ | -------------- | ------------------------ | ------- |
| GET    | `/`            | Danh sách vé của công ty | COMPANY |
| GET    | `/{id}`        | Chi tiết vé              | COMPANY |
| PUT    | `/{id}/status` | Cập nhật trạng thái vé   | COMPANY |
| POST   | `/generate`    | Tạo vé hàng loạt         | COMPANY |

### 7. 💳 Payment API

#### Public Payment API: `/api/payments`

| Method | Endpoint       | Description                    | Role   |
| ------ | -------------- | ------------------------------ | ------ |
| POST   | `/create`      | Tạo thanh toán                 | Public |
| POST   | `/callback`    | Xử lý callback từ gateway      | Public |
| GET    | `/{id}/status` | Kiểm tra trạng thái thanh toán | Public |

#### User Payment API: `/api/user/payments`

| Method | Endpoint | Description         | Role |
| ------ | -------- | ------------------- | ---- |
| GET    | `/`      | Lịch sử thanh toán  | USER |
| GET    | `/{id}`  | Chi tiết thanh toán | USER |

### 8. 🎁 Discount Management API

#### User Discount API: `/api/user/discounts`

| Method | Endpoint     | Description          | Role |
| ------ | ------------ | -------------------- | ---- |
| GET    | `/available` | Mã giảm giá khả dụng | USER |
| POST   | `/validate`  | Kiểm tra mã giảm giá | USER |
| POST   | `/apply`     | Áp dụng mã giảm giá  | USER |

#### Company Discount API: `/api/company/discounts`

| Method | Endpoint | Description                   | Role    |
| ------ | -------- | ----------------------------- | ------- |
| GET    | `/`      | Danh sách mã giảm giá công ty | COMPANY |
| POST   | `/`      | Tạo mã giảm giá               | COMPANY |
| GET    | `/{id}`  | Chi tiết mã giảm giá          | COMPANY |
| PUT    | `/{id}`  | Cập nhật mã giảm giá          | COMPANY |
| DELETE | `/{id}`  | Xóa mã giảm giá               | COMPANY |

#### Admin Discount API: `/api/admin/discounts`

| Method | Endpoint       | Description              | Role  |
| ------ | -------------- | ------------------------ | ----- |
| GET    | `/`            | Tất cả mã giảm giá       | ADMIN |
| POST   | `/platform`    | Tạo mã giảm giá hệ thống | ADMIN |
| PUT    | `/{id}/status` | Cập nhật trạng thái      | ADMIN |

### 9. ⭐ Review Management API

#### User Review API: `/api/user/reviews`

| Method | Endpoint      | Description       | Role |
| ------ | ------------- | ----------------- | ---- |
| POST   | `/`           | Viết đánh giá     | USER |
| GET    | `/my-reviews` | Đánh giá của tôi  | USER |
| PUT    | `/{id}`       | Cập nhật đánh giá | USER |

#### Company Review API: `/api/company/reviews`

| Method | Endpoint | Description         | Role    |
| ------ | -------- | ------------------- | ------- |
| GET    | `/`      | Đánh giá về công ty | COMPANY |
| GET    | `/{id}`  | Chi tiết đánh giá   | COMPANY |

#### Admin Review API: `/api/admin/reviews`

| Method | Endpoint        | Description      | Role  |
| ------ | --------------- | ---------------- | ----- |
| GET    | `/`             | Tất cả đánh giá  | ADMIN |
| PUT    | `/{id}/approve` | Duyệt đánh giá   | ADMIN |
| PUT    | `/{id}/reject`  | Từ chối đánh giá | ADMIN |

### 10. 🚗 Driver Management API

#### User Driver API: `/api/user/drivers`

| Method | Endpoint | Description      | Role |
| ------ | -------- | ---------------- | ---- |
| GET    | `/{id}`  | Thông tin tài xế | USER |

#### Company Driver API: `/api/company/drivers`

| Method | Endpoint           | Description               | Role    |
| ------ | ------------------ | ------------------------- | ------- |
| GET    | `/`                | Danh sách tài xế công ty  | COMPANY |
| POST   | `/`                | Thêm tài xế mới           | COMPANY |
| GET    | `/{id}`            | Chi tiết tài xế           | COMPANY |
| PUT    | `/{id}`            | Cập nhật thông tin tài xế | COMPANY |
| DELETE | `/{id}`            | Xóa tài xế                | COMPANY |
| POST   | `/{id}/assign-bus` | Gán xe cho tài xế         | COMPANY |

### 11. 🏢 Station Management API

#### User Station API: `/api/user/stations`

| Method | Endpoint  | Description      | Role |
| ------ | --------- | ---------------- | ---- |
| GET    | `/`       | Danh sách bến xe | USER |
| GET    | `/{id}`   | Chi tiết bến xe  | USER |
| GET    | `/search` | Tìm kiếm bến xe  | USER |

#### Company Station API: `/api/company/stations`

| Method | Endpoint | Description               | Role    |
| ------ | -------- | ------------------------- | ------- |
| GET    | `/`      | Danh sách bến của công ty | COMPANY |
| POST   | `/`      | Thêm bến xe mới           | COMPANY |
| GET    | `/{id}`  | Chi tiết bến xe           | COMPANY |
| PUT    | `/{id}`  | Cập nhật bến xe           | COMPANY |
| DELETE | `/{id}`  | Xóa bến xe                | COMPANY |

#### Admin Station API: `/api/admin/stations`

| Method | Endpoint        | Description          | Role  |
| ------ | --------------- | -------------------- | ----- |
| GET    | `/`             | Tất cả bến xe        | ADMIN |
| POST   | `/`             | Thêm bến xe hệ thống | ADMIN |
| PUT    | `/{id}/approve` | Duyệt bến xe         | ADMIN |
| PUT    | `/{id}/status`  | Cập nhật trạng thái  | ADMIN |

---

## 🔧 Tính Năng Đặc Biệt

### 1. 🎯 Tự Động Điền Thông Tin

- Hệ thống tự động điền thông tin hành khách từ profile khi đặt vé
- Cho phép người dùng chỉnh sửa thông tin trước khi xác nhận

### 2. 🔒 Khóa Ghế Tạm Thời

- Ghế được khóa trong 5 phút khi người dùng đặt vé
- Tự động giải phóng ghế nếu không thanh toán trong thời hạn

### 3. 📧 Thông Báo Email Tự Động

- Email xác nhận đặt vé
- Email thông báo thanh toán thành công
- Email nhắc nhở thanh toán
- Email thông báo hủy vé
- Email thông báo vé hết hạn

### 4. 💰 Tính Giá Theo Quãng Đường

- Hỗ trợ tuyến đường có nhiều bến trung gian
- Tính giá vé dựa trên khoảng cách giữa các bến

### 5. 🚌 Đa Xe Cho Một Lịch Trình

- Một lịch trình có thể gán nhiều xe
- Quản lý trạng thái từng xe trong lịch trình

### 6. 🎫 Hệ Thống Hủy/Đổi Vé

- Hủy vé với chính sách hoàn tiền linh hoạt
- Đổi vé sang lịch trình khác
- Tính phí hủy/đổi vé tự động

### 7. 📊 Báo Cáo Thống Kê

- Báo cáo doanh thu theo thời gian
- Thống kê vé bán theo trạng thái
- Phân tích hiệu suất tuyến đường

---

## 🛡️ Bảo Mật & Xác Thực

### JWT Token

- Access Token: Thời hạn 24 giờ
- Refresh Token: Thời hạn 7 ngày
- Tự động làm mới token

### Rate Limiting

- Giới hạn số lượng request per IP
- Bảo vệ khỏi tấn công DDoS

### Validation

- Validate tất cả input từ client
- Sanitize dữ liệu trước khi lưu database

---

## 📱 Response Format

### Success Response

```json
{
  "success": true,
  "message": "Thành công",
  "data": {...},
  "timestamp": "2026-03-22T10:30:00"
}
```

### Error Response

```json
{
  "success": false,
  "message": "Lỗi xảy ra",
  "error": "Chi tiết lỗi",
  "timestamp": "2026-03-22T10:30:00"
}
```

### Pagination Response

```json
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

---

## 🚀 Deployment & Environment

### Database

- **Production**: PostgreSQL
- **Development**: H2/PostgreSQL
- **Migration**: Flyway

### Caching

- Redis cho session và cache

### File Storage

- Cloudinary cho upload ảnh

### Email Service

- SMTP configuration
- HTML email templates

---

## 📞 Support & Contact

Để được hỗ trợ kỹ thuật, vui lòng liên hệ:

- **Email**: support@datveexe.com
- **Documentation**: [API Docs](http://localhost:8080/swagger-ui.html)
- **Version**: 1.0.0
- **Last Updated**: March 22, 2026

---

_Tài liệu này được cập nhật thường xuyên. Vui lòng kiểm tra phiên bản mới nhất._
