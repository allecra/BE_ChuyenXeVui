# CK DatVeXe Backend

Spring Boot Backend application for CK DatVeXe project.

## Project Structure

```
src/
├── main/
│   ├── java/com/example/ckdatveexe/
│   │   ├── config/              # Configuration classes (Security, JWT, Database, etc.)
│   │   ├── exception/           # Custom exceptions and global exception handler
│   │   ├── module/              # Feature modules
│   │   │   ├── auth/            # Authentication module
│   │   │   │   ├── controller/  # Auth controllers
│   │   │   │   ├── dto/         # Auth DTOs
│   │   │   │   └── service/     # Auth services
│   │   │   ├── buscompany/      # Bus company management module
│   │   │   │   ├── controller/  # Bus company controllers (Admin & User)
│   │   │   │   ├── dto/         # Bus company DTOs
│   │   │   │   └── service/     # Bus company services
│   │   │   ├── controller/      # General controllers
│   │   │   └── media/           # Media upload module
│   │   ├── shared/              # Shared components
│   │   │   ├── dto/             # Common DTOs
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── guard/           # Security guards
│   │   │   └── repository/      # Data access layer
│   │   └── CkDatVeXeApplication.java
│   └── resources/
│       ├── application.yml      # Application configuration
│       └── db/migration/        # Database migration files
└── test/
    └── java/com/example/ckdatveexe/
```

## Requirements

- Java 17+
- Maven 3.6+
- MySQL 5.7+

## Setup & Configuration

### 1. Database Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ck_datveexe
    username: root
    password: your_password
```

### 2. Environment Configuration

Copy `.env.example` to `.env` and configure:

```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ck_datveexe
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### 3. Build Project

```bash
mvn clean install
```

### 4. Run Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Features

### Authentication & Authorization

- JWT-based authentication
- Role-based access control (USER, ADMIN)
- Password reset with OTP verification
- Refresh token mechanism

### Bus Company Management

- **User Features:**
  - View list of bus companies with search and pagination
  - Register new bus company (requires approval)
  - Receive email notifications about registration status

- **Admin Features:**
  - Full CRUD operations for bus companies
  - Review and approve/reject bus company registrations
  - Send email notifications to applicants
  - Advanced filtering and search capabilities

### Email Notifications

- Registration confirmation emails
- Approval/rejection notifications
- Password reset emails
- Customizable email templates

### Media Upload

- Cloudinary integration for image uploads
- Support for various image formats
- Automatic image optimization

### Security Features

- Input validation and sanitization
- SQL injection prevention
- XSS protection
- CORS configuration
- Rate limiting (if configured)

## API Endpoints

### Health Check

- `GET /api/health` - Check API status

### Authentication

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/verify-otp` - Verify OTP code
- `POST /api/auth/reset-password` - Reset password with new password
- `POST /api/auth/refresh-token` - Refresh access token

### Bus Company Management

#### User APIs (`/api/user/bus-companies`)

- `GET /api/user/bus-companies` - Lấy danh sách nhà xe (có phân trang và tìm kiếm)
- `GET /api/user/bus-companies/{id}` - Lấy thông tin chi tiết nhà xe
- `POST /api/user/bus-companies/register` - Đăng ký nhà xe mới (cần xác thực từ admin)

#### Admin APIs (`/api/admin/bus-companies`)

- `GET /api/admin/bus-companies` - Lấy danh sách nhà xe (admin)
- `GET /api/admin/bus-companies/{id}` - Lấy thông tin chi tiết nhà xe
- `POST /api/admin/bus-companies` - Tạo nhà xe mới
- `PUT /api/admin/bus-companies/{id}` - Cập nhật thông tin nhà xe
- `DELETE /api/admin/bus-companies/{id}` - Xóa nhà xe

#### Registration Management (Admin only)

- `GET /api/admin/bus-companies/registrations` - Lấy danh sách đăng ký nhà xe
- `GET /api/admin/bus-companies/registrations/{id}` - Lấy chi tiết đăng ký
- `POST /api/admin/bus-companies/registrations/{id}/approve` - Duyệt đăng ký
- `POST /api/admin/bus-companies/registrations/{id}/reject` - Từ chối đăng ký

### Media Management

- `POST /api/media/upload` - Upload file lên Cloudinary

## Dependencies

- Spring Boot Web
- Spring Data JPA
- Spring Security
- Spring Boot Mail (for email notifications)
- MySQL Connector
- Lombok
- Validation
- JWT (JSON Web Token)
- Cloudinary (for media upload)
- Swagger/OpenAPI (for API documentation)

## Database Schema

### Core Tables

- `users` - User accounts and authentication
- `roles` - User roles (USER, ADMIN)
- `refresh_tokens` - JWT refresh tokens
- `password_resets` - Password reset tokens

### Bus Company Tables

- `bus_companies` - Thông tin nhà xe đã được duyệt
- `bus_company_registrations` - Đăng ký nhà xe chờ xử lý

### Other Tables

- `buses` - Bus information
- `routes` - Bus routes
- `schedules` - Bus schedules
- `tickets` - Ticket bookings
- `payments` - Payment records

## API Documentation

Once the application is running, you can access the API documentation at:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

```bash
mvn test
```

## Usage Examples

# Bus Company Management API

## Tổng quan

Module này cung cấp CRUD đầy đủ cho quản lý nhà xe với workflow đăng ký và duyệt hoàn chỉnh:

- **BusCompanyUserController**: API cho người dùng
- **BusCompanyAdminController**: API cho admin với quản lý đăng ký

## Tính năng chính

### 1. Quản lý nhà xe (CRUD)

- Tạo, đọc, cập nhật, xóa nhà xe
- Tìm kiếm và phân trang
- Chỉ admin mới có quyền tạo/sửa/xóa trực tiếp

### 2. Workflow đăng ký nhà xe

- User đăng ký nhà xe → Lưu vào `bus_company_registrations` với status PENDING
- Gửi email xác nhận đăng ký
- Admin xem xét và duyệt/từ chối
- Gửi email thông báo kết quả
- Nếu được duyệt → Tự động tạo `BusCompany`

## API Endpoints

### User APIs (`/api/user/bus-companies`)

#### 1. Lấy danh sách nhà xe

```
GET /api/user/bus-companies
```

**Parameters:**

- `companyName` (optional): Tên nhà xe để tìm kiếm
- `page` (default: 0): Số trang
- `size` (default: 10): Số lượng bản ghi mỗi trang
- `sortBy` (default: createdAt): Trường sắp xếp
- `sortDir` (default: desc): Hướng sắp xếp

#### 2. Lấy thông tin nhà xe

```
GET /api/user/bus-companies/{id}
```

#### 3. Đăng ký nhà xe

```
POST /api/user/bus-companies/register
```

**Yêu cầu:** Role USER
**Body:**

```json
{
  "companyName": "Nhà xe ABC",
  "email": "contact@nhaxeabc.com",
  "phoneNumber": "0123456789",
  "image": "https://example.com/logo.jpg",
  "descriptions": "Mô tả về nhà xe",
  "businessLicense": "Số giấy phép kinh doanh",
  "address": "Địa chỉ nhà xe"
}
```

### Admin APIs (`/api/admin/bus-companies`)

#### 1. CRUD nhà xe

```
GET /api/admin/bus-companies          # Lấy danh sách
GET /api/admin/bus-companies/{id}     # Lấy chi tiết
POST /api/admin/bus-companies         # Tạo mới
PUT /api/admin/bus-companies/{id}     # Cập nhật
DELETE /api/admin/bus-companies/{id}  # Xóa
```

#### 2. Quản lý đăng ký

```
GET /api/admin/bus-companies/registrations              # Lấy danh sách đăng ký
GET /api/admin/bus-companies/registrations/{id}        # Lấy chi tiết đăng ký
POST /api/admin/bus-companies/registrations/{id}/approve # Duyệt đăng ký
POST /api/admin/bus-companies/registrations/{id}/reject  # Từ chối đăng ký
```

**Body cho approve/reject:**

```json
{
  "adminNotes": "Ghi chú của admin"
}
```

## Quy trình đăng ký nhà xe

1. **User đăng ký:** Gửi thông tin đăng ký qua API `/api/user/bus-companies/register`
2. **Email xác nhận:** Hệ thống gửi email xác nhận đã nhận đăng ký
3. **Admin xem xét:** Admin xem danh sách đăng ký và chi tiết
4. **Duyệt/Từ chối:** Admin duyệt hoặc từ chối đăng ký
5. **Email thông báo:** Hệ thống gửi email thông báo kết quả
6. **Tạo nhà xe:** Nếu được duyệt, hệ thống tự động tạo BusCompany

## Cấu trúc Database

### Bảng `bus_companies`

- `id`: ID nhà xe
- `company_name`: Tên nhà xe
- `image`: Hình ảnh logo
- `descriptions`: Mô tả
- `created_at`, `updated_at`: Thời gian tạo/cập nhật

### Bảng `bus_company_registrations`

- `id`: ID đăng ký
- `company_name`: Tên nhà xe
- `email`: Email liên hệ
- `phone_number`: Số điện thoại
- `image`: Logo
- `descriptions`: Mô tả
- `business_license`: Giấy phép kinh doanh
- `address`: Địa chỉ
- `status`: Trạng thái (PENDING/APPROVED/REJECTED)
- `admin_notes`: Ghi chú của admin
- `approved_by`: ID admin duyệt
- `approved_at`: Thời gian duyệt
- `created_at`, `updated_at`: Thời gian tạo/cập nhật

## Email Templates

### 1. Email xác nhận đăng ký

Gửi khi user đăng ký thành công, thông báo đã nhận đơn và đang xem xét.

### 2. Email thông báo duyệt

Gửi khi admin duyệt đăng ký, chúc mừng và hướng dẫn bước tiếp theo.

### 3. Email thông báo từ chối

Gửi khi admin từ chối đăng ký, kèm lý do và hướng dẫn đăng ký lại.

## Validation

### BusCompanyCreateRequest

- `companyName`: Bắt buộc, tối đa 255 ký tự
- `descriptions`: Tối đa 5000 ký tự

### BusCompanyRegistrationRequest

- `companyName`: Bắt buộc, tối đa 255 ký tự
- `email`: Bắt buộc, định dạng email hợp lệ
- `phoneNumber`: Bắt buộc
- `descriptions`: Tối đa 5000 ký tự

## Security

- User APIs: Một số endpoint yêu cầu role USER
- Admin APIs: Tất cả endpoint yêu cầu role ADMIN
- Validation đầu vào để tránh SQL injection và XSS
- Kiểm tra trùng lặp tên công ty và email

## Error Handling

- `ResourceNotFoundException`: Không tìm thấy resource
- `IllegalArgumentException`: Dữ liệu không hợp lệ (trùng lặp, v.v.)
- Validation errors: Lỗi validate đầu vào
- Email errors: Lỗi gửi email (log nhưng không fail transaction)

## Examples

### Đăng ký nhà xe (User)

```bash
curl -X POST "http://localhost:8080/api/user/bus-companies/register" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer USER_JWT_TOKEN" \
  -d '{
    "companyName": "Nhà xe Phương Trang",
    "email": "contact@phuongtrang.vn",
    "phoneNumber": "0283 8386 852",
    "businessLicense": "0123456789",
    "address": "272 Đề Thám, Q1, TP.HCM"
  }'
```

### Duyệt đăng ký (Admin)

```bash
curl -X POST "http://localhost:8080/api/admin/bus-companies/registrations/1/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -d '{
    "adminNotes": "Đăng ký hợp lệ, đã kiểm tra giấy phép"
  }'
```

### Lấy danh sách đăng ký chờ duyệt (Admin)

```bash
curl -X GET "http://localhost:8080/api/admin/bus-companies/registrations?status=PENDING&page=0&size=10" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

## License

All rights reserved
