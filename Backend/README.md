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
│   │   │   ├── bus/             # Bus management module
│   │   │   │   ├── controller/  # Bus controllers (User, Admin, Company)
│   │   │   │   ├── dto/         # Bus DTOs
│   │   │   │   └── service/     # Bus services
│   │   │   ├── seat/            # Seat management module
│   │   │   │   ├── controller/  # Seat controllers (User, Company)
│   │   │   │   ├── dto/         # Seat DTOs
│   │   │   │   └── service/     # Seat services
│   │   │   ├── station/         # Station management module
│   │   │   │   ├── controller/  # Station controllers (User, Company)
│   │   │   │   ├── dto/         # Station DTOs
│   │   │   │   └── service/     # Station services
│   │   │   ├── buscompany/      # Bus company management module
│   │   │   │   ├── controller/  # Bus company controllers (Admin & User)
│   │   │   │   ├── dto/         # Bus company DTOs
│   │   │   │   └── service/     # Bus company services
│   │   │   ├── user/            # User management module
│   │   │   │   ├── controller/  # User controllers (Admin)
│   │   │   │   ├── dto/         # User DTOs
│   │   │   │   └── service/     # User services
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

# Cloudinary Configuration (Required for image upload)
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
- Role-based access control (USER, ADMIN, BUS_COMPANY)
- Password reset with OTP verification
- Refresh token mechanism

### Bus Management System

- **Complete Bus CRUD Operations:**
  - Create buses with automatic seat generation
  - Update bus information
  - Soft delete (move to deleted_buses table) and hard delete
  - Restore deleted buses
  - View deleted buses history

- **Automatic Seat Generation:**
  - **Ghế Ngồi (GHE_NGOI)**: 4-column layout with normal seats
  - **Giường Nằm (GIUONG_NAM)**: 6-bed layout with sleeper beds
  - **Limousine (LIMOUSINE)**: 3-column layout with VIP seats

- **Seat Layout Management:**
  - Customize seat arrangements
  - Update seat pricing
  - Regenerate seats based on bus type

- **Role-based Bus Access:**
  - **USER**: View active buses and search
  - **ADMIN**: Full access to all buses with detailed information
  - **BUS_COMPANY**: Complete CRUD for own buses only

### Seat Management System

- **Role-based Seat Operations:**
  - **USER**: View available seats, check seat details
  - **BUS_COMPANY**: Full CRUD operations for seats on own buses

- **Seat Features:**
  - Soft delete functionality (DELETED status)
  - Price management per seat
  - Status management (AVAILABLE, BOOKED, MAINTENANCE, DELETED)
  - Business rules enforcement (can't delete/update BOOKED seats)

- **Seat Types:**
  - **STANDARD**: Regular seats
  - **VIP**: Premium seats
  - **SLEEPER**: Sleeping beds
  - **BUSINESS**: Business class seats

### Station Management System

- **Multi-role Station Access:**
  - **USER**: View stations and active buses at stations
  - **BUS_COMPANY**: Full CRUD operations, bus assignment

- **Station Features:**
  - Many-to-many Bus-Station relationships
  - Assign/remove buses to/from stations
  - Soft/hard delete functionality
  - Advanced search and filtering

- **Bus Assignment:**
  - Flexible bus-station assignments
  - Replace all buses or add new ones
  - Track bus counts per station

### User Management System

- **Admin User Management:**
  - View all users with filtering
  - Update user information and status
  - Soft delete users with restoration capability
  - User status management (ACTIVE, BLOCKED, PENDING)

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
  - Reset bus company passwords
  - View which company owns specific buses
  - Delete bus companies with email notifications

- **Bus Company Features:**
  - View own company information
  - View and manage own buses with detailed seat information
  - Change password with email notification
  - Access to bus management APIs

### Password Management

- **Admin Password Reset**: Reset any bus company password
- **Self Password Change**: Bus companies can change their own passwords
- **Email Notifications**: Automatic email notifications for all password changes
- **Security Validation**: Current password verification for changes

### Email Notification System

- Registration confirmation emails
- Approval/rejection notifications
- Password reset notifications
- Password change confirmations
- Account creation notifications with temporary passwords
- Professional HTML email templates

### Media Upload

- Cloudinary integration for image uploads
- Support for various image formats (jpg, png, gif, webp, etc.)
- Automatic image optimization and transformation
- Folder-based organization on Cloudinary
- File size validation (max 10MB)
- Secure URL generation for database storage

**Workflow:**

1. Frontend selects image file from local folder
2. Upload via `POST /api/media/upload` with optional folder parameter
3. Cloudinary processes and stores the image
4. API returns secure URL and metadata
5. Save the returned URL to database entity (bus.image, station.image, etc.)

**Supported Use Cases:**

- Bus images and galleries
- Station photos and wallpapers
- Company logos and branding
- User profile pictures
- General media assets

### Security Features

- Input validation and sanitization
- SQL injection prevention
- XSS protection
- CORS configuration
- Role-based API access control
- Password strength validation

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

### Bus Management APIs

#### User APIs (`/api/user/buses`) - **Không yêu cầu đăng nhập**

- `GET /api/user/buses` - Xem tất cả xe khả dụng
- `GET /api/user/buses/{busId}` - Xem chi tiết xe
- `GET /api/user/buses/search` - Tìm kiếm xe
- `GET /api/user/buses/company/{companyId}` - Xe của nhà xe cụ thể

#### Admin APIs (`/api/admin/buses`)

- `GET /api/admin/buses` - Xem tất cả xe (có filter)
- `GET /api/admin/buses/{busId}` - Chi tiết xe (bao gồm ghế)
- `PUT /api/admin/buses/{busId}` - Cập nhật xe
- `GET /api/admin/buses/company/{companyId}` - Xe của nhà xe

#### Bus Company APIs (`/api/bus-company/buses`)

- `POST /api/bus-company/buses` - Tạo xe mới (tự động sinh ghế)
- `GET /api/bus-company/buses` - Danh sách xe của nhà xe
- `GET /api/bus-company/buses/{busId}` - Chi tiết xe
- `PUT /api/bus-company/buses/{busId}` - Cập nhật xe
- `DELETE /api/bus-company/buses/{busId}` - Xóa xe (soft/hard delete)
- `PUT /api/bus-company/buses/{busId}/seat-layout` - Sắp xếp sơ đồ ghế
- `POST /api/bus-company/buses/{busId}/regenerate-seats` - Tái tạo ghế
- `GET /api/bus-company/buses/deleted` - Xem xe đã xóa
- `POST /api/bus-company/buses/deleted/{deletedBusId}/restore` - Khôi phục xe

### Seat Management APIs

#### User APIs (`/api/user/seats`) - **Yêu cầu đăng nhập**

- `GET /api/user/seats/bus/{busId}` - Danh sách ghế của xe (chỉ ghế không bị xóa)
- `GET /api/user/seats/{seatId}` - Chi tiết ghế
- `GET /api/user/seats/available/bus/{busId}` - Ghế trống có thể đặt

#### Bus Company APIs (`/api/bus-company/seats`)

- `POST /api/bus-company/seats` - Tạo ghế mới
- `GET /api/bus-company/seats` - Danh sách tất cả ghế (bao gồm đã xóa)
- `GET /api/bus-company/seats/{seatId}` - Chi tiết ghế
- `PUT /api/bus-company/seats/{seatId}` - Cập nhật thông tin ghế
- `PUT /api/bus-company/seats/{seatId}/status` - Cập nhật trạng thái ghế
- `PUT /api/bus-company/seats/{seatId}/price` - Cập nhật giá ghế
- `DELETE /api/bus-company/seats/{seatId}` - Xóa mềm ghế

### Station Management APIs

#### User APIs (`/api/user/stations`) - **Yêu cầu đăng nhập**

- `GET /api/user/stations` - Danh sách tất cả bến xe (có phân trang, lọc)
- `GET /api/user/stations/{stationId}` - Chi tiết bến xe
- `GET /api/user/stations/{stationId}/buses` - Danh sách xe tại bến (chỉ xe ACTIVE)
- `GET /api/user/stations/search` - Tìm kiếm bến xe theo từ khóa
- `GET /api/user/stations/{stationId}/buses/search` - Tìm kiếm xe trong bến

#### Bus Company APIs (`/api/bus-company/stations`)

- `POST /api/bus-company/stations` - Tạo bến xe mới
- `GET /api/bus-company/stations` - Danh sách tất cả bến xe (full details)
- `GET /api/bus-company/stations/{stationId}` - Chi tiết bến xe
- `PUT /api/bus-company/stations/{stationId}` - Cập nhật thông tin bến xe
- `POST /api/bus-company/stations/assign-buses` - Gắn xe vào bến
- `DELETE /api/bus-company/stations/{stationId}/buses` - Gỡ xe khỏi bến
- `GET /api/bus-company/stations/{stationId}/buses` - Danh sách xe tại bến (tất cả trạng thái)
- `GET /api/bus-company/stations/search` - Tìm kiếm bến xe
- `GET /api/bus-company/stations/{stationId}/buses/search` - Tìm kiếm xe trong bến
- `DELETE /api/bus-company/stations/{stationId}` - Xóa bến xe (soft/hard delete)

### User Management APIs

#### Admin APIs (`/api/admin/users`)

- `GET /api/admin/users` - Danh sách tất cả người dùng (có phân trang, lọc)
- `GET /api/admin/users/{userId}` - Chi tiết người dùng
- `PUT /api/admin/users/{userId}` - Cập nhật thông tin người dùng
- `PUT /api/admin/users/{userId}/status` - Cập nhật trạng thái người dùng
- `DELETE /api/admin/users/{userId}` - Xóa mềm người dùng
- `GET /api/admin/users/deleted` - Danh sách người dùng đã xóa
- `POST /api/admin/users/deleted/{deletedUserId}/restore` - Khôi phục người dùng

### Bus Company Management APIs

#### Bus Company Management (`/api/bus-company/management`)

- `GET /api/bus-company/management/my-company` - Thông tin nhà xe của tôi
- `GET /api/bus-company/management/company-buses` - Danh sách xe của nhà xe
- `GET /api/bus-company/management/company-buses/{busId}` - Chi tiết xe (có ghế)
- `POST /api/bus-company/management/change-password` - Đổi mật khẩu

#### Admin Bus Company APIs (`/api/admin/bus-company`)

- `GET /api/admin/bus-company/bus-owner?busId={busId}` - Xe thuộc nhà xe nào
- `POST /api/admin/bus-company/reset-password` - Reset mật khẩu nhà xe

### Bus Company Registration APIs

### Bus Company Registration (Public API)

#### Public Registration API (`/api/public/bus-company`)

- `POST /api/public/bus-company/register` - Đăng ký nhà xe (không yêu cầu đăng nhập)
- `GET /api/public/bus-company/registration-status/{email}` - Kiểm tra trạng thái đăng ký

#### User APIs (`/api/user/bus-companies`) - **Không yêu cầu đăng nhập**

- `GET /api/user/bus-companies` - Lấy danh sách nhà xe (có phân trang và tìm kiếm)
- `GET /api/user/bus-companies/{id}` - Lấy thông tin chi tiết nhà xe
- `GET /api/user/bus-companies/search` - Tìm kiếm nhà xe theo từ khóa

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

- `POST /api/media/upload` - Upload file lên Cloudinary với folder tùy chọn

#### Media Upload API Details

**Endpoint:** `POST /api/media/upload`
**Content-Type:** `multipart/form-data`
**Authentication:** Required (JWT Bearer token)
**Roles:** USER, ADMIN, BUS_COMPANY

**Parameters:**

- `file` (required): File ảnh từ folder local
- `folder` (optional): Tên folder trên Cloudinary (default: "uploads")

**File Validation:**

- Chỉ chấp nhận file hình ảnh (jpg, png, gif, etc.)
- Kích thước tối đa: 10MB
- File không được để trống

**Response Format:**

```json
{
  "success": true,
  "message": "Upload file thành công",
  "data": {
    "url": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/folder/filename.jpg",
    "publicId": "folder/filename",
    "format": "jpg",
    "resourceType": "image",
    "bytes": 123456
  }
}
```

**Usage Example:**

```bash
curl -X POST "http://localhost:8080/api/media/upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "folder=bus-images"
```

**Integration with Other APIs:**

- Bus images: Use `folder=bus-images`
- Station images: Use `folder=station-images`
- Company logos: Use `folder=company-logos`
- User avatars: Use `folder=user-avatars`

The returned `url` should be saved to the database in the respective entity's image field.

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
- `roles` - User roles (USER, ADMIN, BUS_COMPANY)
- `refresh_tokens` - JWT refresh tokens
- `password_resets` - Password reset tokens

### Bus Company Tables

- `bus_companies` - Thông tin nhà xe đã được duyệt
- `bus_company_registrations` - Đăng ký nhà xe chờ xử lý

### Bus & Transportation Tables

- `buses` - Bus information
- `seats` - Bus seat information with layout and pricing
- `deleted_buses` - Soft deleted buses with restoration capability
- `stations` - Bus stations and terminals
- `routes` - Bus routes
- `schedules` - Bus schedules

### User Management Tables

- `deleted_users` - Soft deleted users with restoration capability

### Booking & Payment Tables

- `tickets` - Ticket bookings
- `payments` - Payment records

### Key Enums

#### SeatStatus

- `AVAILABLE` - Seat is available for booking
- `BOOKED` - Seat is already booked
- `MAINTENANCE` - Seat is under maintenance
- `DELETED` - Seat is soft deleted

#### SeatType

- `STANDARD` - Regular seats
- `VIP` - Premium seats
- `SLEEPER` - Sleeping beds
- `BUSINESS` - Business class seats

#### BusStatus

- `ACTIVE` - Bus is operational
- `MAINTENANCE` - Bus is under maintenance
- `INACTIVE` - Bus is not operational

#### UserStatus

- `ACTIVE` - User account is active
- `BLOCKED` - User account is blocked
- `PENDING` - User account is pending activation

### Database Migrations

The system uses Flyway migrations for database schema management:

- `V1__Create_bus_company_registration_table.sql` - Create bus company registration table
- `V2__Create_bus_and_seat_tables.sql` - Create bus and seat tables
- `V3__Update_seat_type_enum.sql` - Update seat type enum
- `V4__Add_bus_company_role.sql` - Add BUS_COMPANY role
- `V5__Update_phone_column_length.sql` - Increase phone column length
- `V6__Fix_seat_type_enum.sql` - Fix SeatType enum values
- `V7__Update_seat_status_enum.sql` - Add MAINTENANCE, DELETED to SeatStatus
- `V8__Update_password_reset_otp_length.sql` - Update password reset OTP length

## API Documentation

Once the application is running, you can access the API documentation at:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

```bash
mvn test
```

## Usage Examples

### Media Upload Integration

#### 1. Upload Bus Image

```bash
# Step 1: Upload image to Cloudinary
curl -X POST "http://localhost:8080/api/media/upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@bus-photo.jpg" \
  -F "folder=bus-images"

# Response:
{
  "success": true,
  "data": {
    "url": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/bus-images/bus-photo.jpg"
  }
}

# Step 2: Create bus with the uploaded image URL
curl -X POST "http://localhost:8080/api/bus-company/buses" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Xe Limousine VIP",
    "licensePlate": "51A-12345",
    "capacity": 22,
    "busType": "LIMOUSINE",
    "image": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/bus-images/bus-photo.jpg"
  }'
```

#### 2. Upload Station Image

```bash
# Upload station image
curl -X POST "http://localhost:8080/api/media/upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@station-photo.jpg" \
  -F "folder=station-images"

# Create station with uploaded image
curl -X POST "http://localhost:8080/api/bus-company/stations" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bến xe Miền Đông",
    "location": "TP.HCM",
    "image": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/station-images/station-photo.jpg"
  }'
```

#### 3. Upload Company Logo

```bash
# Upload company logo
curl -X POST "http://localhost:8080/api/media/upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@company-logo.png" \
  -F "folder=company-logos"

# Register bus company with logo
curl -X POST "http://localhost:8080/api/public/bus-company/register" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Nhà xe Phương Trang",
    "email": "contact@phuongtrang.vn",
    "image": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/company-logos/company-logo.png"
  }'
```

### Recommended Folder Structure on Cloudinary

```
uploads/
├── bus-images/          # Bus photos and galleries
├── station-images/      # Station photos and wallpapers
├── company-logos/       # Bus company logos and branding
├── user-avatars/        # User profile pictures
└── general/            # Other media assets
```

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

### Đăng ký nhà xe (Public)

```bash
curl -X POST "http://localhost:8080/api/public/bus-company/register" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Nhà xe Phương Trang",
    "email": "contact@phuongtrang.vn",
    "phoneNumber": "0283 8386 852",
    "businessLicense": "0123456789",
    "address": "272 Đề Thám, Q1, TP.HCM"
  }'
```

### Kiểm tra trạng thái đăng ký

```bash
curl -X GET "http://localhost:8080/api/public/bus-company/registration-status/contact@phuongtrang.vn"
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

## Current System Status

### ✅ Completed Modules

- **Authentication System**: Complete JWT-based auth with role management
- **Bus Management**: Full CRUD with automatic seat generation and soft delete
- **Seat Management**: Complete seat lifecycle with business rules
- **Station Management**: Full station and bus assignment system
- **User Management**: Complete user lifecycle with admin controls
- **Bus Company Management**: Registration workflow with admin approval
- **Email System**: Professional HTML templates with automated notifications

### 📊 System Statistics

- **API Endpoints**: 50+ endpoints across 6 modules
- **Database Tables**: 15+ entities with relationships
- **Migration Files**: 8 Flyway migrations
- **Roles**: 3 role-based access levels (USER, ADMIN, BUS_COMPANY)
- **Email Templates**: 10+ professional email templates
- **Status Codes**: Comprehensive HTTP status code handling

### 🔧 Technical Features

- **Security**: JWT authentication, role-based authorization, input validation
- **Database**: MySQL with Flyway migrations, soft delete capabilities
- **Email**: SMTP integration with HTML templates
- **File Upload**: Cloudinary integration for media management
- **Documentation**: Complete Swagger/OpenAPI documentation
- **Logging**: Comprehensive logging with emoji status indicators
- **Error Handling**: Global exception handling with Vietnamese messages

### 🚀 Production Ready

The system is fully functional and production-ready with:

- Complete CRUD operations for all entities
- Role-based security implementation
- Professional email notification system
- Comprehensive error handling and validation
- Database migration support
- API documentation
- Logging and monitoring capabilities

**Current Version**: 3.0.0  
**Last Updated**: February 2025  
**Status**: Production Ready ✅

## Recent Updates

### Version 3.0 - Complete API System with Station & Seat Management

#### 🏢 Station Management System

- **Multi-role Station Access**: USER can view stations and buses, BUS_COMPANY has full CRUD
- **Bus-Station Relationships**: Many-to-many relationships with flexible assignment
- **Advanced Search**: Search stations by keyword and location with pagination
- **Soft/Hard Delete**: Safe deletion with restoration capabilities

#### 🪑 Comprehensive Seat Management

- **Role-based Seat Operations**: USER view-only access, BUS_COMPANY full CRUD
- **Seat Status Management**: AVAILABLE, BOOKED, MAINTENANCE, DELETED states
- **Business Rules**: Prevent deletion/modification of BOOKED seats
- **Price Management**: Individual seat pricing with bulk updates

#### � User Management System

- **Admin User Control**: Full user lifecycle management
- **User Status Management**: ACTIVE, BLOCKED, PENDING states
- **Soft Delete & Restore**: Safe user deletion with restoration
- **Advanced Filtering**: Search and filter users by various criteria

#### 🚌 Enhanced Bus Management

- **Complete CRUD Operations**: Full lifecycle management for buses
- **Automatic Seat Generation**: Smart seat layouts based on bus type
- **Soft Delete & Restore**: Safe deletion with restoration capabilities
- **Advanced Search**: Multi-criteria search and filtering

#### 🔐 Enhanced Security & Authentication

- **Role-based Access Control**: USER, ADMIN, BUS_COMPANY roles
- **JWT Authentication**: Secure token-based authentication
- **Password Management**: Reset and change password functionality
- **Email Notifications**: Automated email notifications for all actions

#### � Professional Email System

- **HTML Email Templates**: Beautiful, responsive email templates
- **Multiple Notification Types**: Registration, approval, password changes, deletions
- **Automatic Triggers**: Smart email sending based on system events
- **Vietnamese Language Support**: All emails in Vietnamese

#### �️ Database Enhancements

- **8 Migration Files**: Complete database schema evolution
- **Soft Delete Tables**: `deleted_buses`, `deleted_users` for safe deletion
- **Enhanced Enums**: Complete status management for all entities
- **Data Integrity**: Foreign key relationships and constraints

#### 🛡️ Business Rules & Validation

- **Seat Management**: Cannot delete/update BOOKED seats
- **User Management**: Proper status transitions and validations
- **Station Management**: Bus assignment validation and constraints
- **Company Management**: Registration workflow with approval process

#### 📊 API Documentation & Monitoring

- **Comprehensive Logging**: Emoji-based status code logging
- **Error Handling**: Proper HTTP status codes with Vietnamese messages
- **API Documentation**: Complete Swagger documentation
- **Performance Monitoring**: Request/response tracking

### Version 2.0 - Bus Management System & Enhanced Features

#### 🚌 Complete Bus Management System

- **Full CRUD Operations**: Create, read, update, delete buses with role-based access
- **Automatic Seat Generation**: Smart seat layout generation based on bus type
- **Soft Delete & Restore**: Safe deletion with ability to restore deleted buses
- **Seat Layout Customization**: Flexible seat arrangement and pricing management

#### 🔐 Enhanced Security & Password Management

- **Self Password Change**: Bus companies can securely change their passwords
- **Admin Password Reset**: Administrators can reset bus company passwords
- **Email Notifications**: Automatic notifications for all password-related activities
- **Account Creation**: Automatic user account creation when bus company registration is approved

#### 🏢 Advanced Bus Company Features

- **Company Dashboard**: Bus companies can view their own information and buses
- **Detailed Bus Information**: Complete bus details including seat layouts
- **Bus Ownership Tracking**: Administrators can see which company owns any bus
- **Registration Workflow**: Streamlined registration with automatic account creation

# Bus API Documentation

## Tổng quan

API Bus được thiết kế theo 3 role chính:

- **USER**: Xem thông tin xe, tìm kiếm xe
- **ADMIN**: Quản lý tất cả xe với quyền cao nhất
- **BUS_COMPANY**: CRUD đầy đủ cho xe của nhà xe

## Cấu trúc API

### 1. User APIs (`/api/user/buses`)

**Role**: `ROLE_USER`

#### GET `/api/user/buses`

- **Mô tả**: Xem tất cả các xe khả dụng
- **Parameters**:
  - `keyword` (optional): Từ khóa tìm kiếm
  - `page`, `size`: Phân trang
  - `sortBy`, `sortDirection`: Sắp xếp

#### GET `/api/user/buses/{busId}`

- **Mô tả**: Xem chi tiết xe
- **Response**: Thông tin cơ bản của xe, không bao gồm thông tin nhạy cảm

#### GET `/api/user/buses/search`

- **Mô tả**: Tìm kiếm xe theo từ khóa
- **Parameters**: `keyword` (required)

#### GET `/api/user/buses/company/{companyId}`

- **Mô tả**: Xem xe của một nhà xe cụ thể

### 2. Admin APIs (`/api/admin/buses`)

**Role**: `ROLE_ADMIN`

#### GET `/api/admin/buses`

- **Mô tả**: Xem tất cả xe với thông tin chi tiết
- **Parameters**: Hỗ trợ filter theo `busType`, `status`, `companyId`

#### GET `/api/admin/buses/{busId}`

- **Mô tả**: Xem chi tiết xe (bao gồm thông tin ghế)

#### PUT `/api/admin/buses/{busId}`

- **Mô tả**: Cập nhật thông tin xe
- **Body**: `BusUpdateRequest`

#### GET `/api/admin/buses/company/{companyId}`

- **Mô tả**: Xem xe của nhà xe cụ thể

### 3. Bus Company APIs (`/api/bus-company/buses`)

**Role**: `ROLE_BUS_COMPANY`

#### POST `/api/bus-company/buses`

- **Mô tả**: Tạo xe mới và tự động sinh ghế
- **Body**: `BusCreateRequest`
- **Features**:
  - Tự động sinh ghế theo loại xe
  - Kiểm tra biển số trùng

#### GET `/api/bus-company/buses`

- **Mô tả**: Danh sách xe của nhà xe

#### GET `/api/bus-company/buses/{busId}`

- **Mô tả**: Chi tiết xe của nhà xe

#### PUT `/api/bus-company/buses/{busId}`

- **Mô tả**: Cập nhật xe
- **Body**: `BusUpdateRequest`

#### DELETE `/api/bus-company/buses/{busId}`

- **Mô tả**: Xóa xe (hỗ trợ cả xóa cứng và mềm)
- **Body**: `DeleteBusRequest`
  - `hardDelete: false` → Xóa mềm (lưu vào `deleted_buses`)
  - `hardDelete: true` → Xóa cứng (xóa khỏi database)

#### PUT `/api/bus-company/buses/{busId}/seat-layout`

- **Mô tả**: Sắp xếp sơ đồ ghế
- **Body**: `SeatLayoutRequest`

#### POST `/api/bus-company/buses/{busId}/regenerate-seats`

- **Mô tả**: Tái tạo ghế theo loại xe

## Tự động sinh ghế theo loại xe

### 1. Xe Ghế Ngồi (`GHE_NGOI`)

- **Layout**: 4 cột (2-2 với lối đi giữa)
- **Ghế**: Loại `NORMAL`
- **Giá**: 100,000 VNĐ
- **Sơ đồ**:

```
A01 A02    A03 A04
A05 A06    A07 A08
...
```

### 2. Xe Giường Nằm (`GIUONG_NAM`)

- **Layout**: 6 giường/hàng (2 tầng mỗi bên + 1 tầng giữa)
- **Ghế**: `SLEEPER_LOWER` (150k), `SLEEPER_UPPER` (140k)
- **Sơ đồ**:

```
T01(dưới) T02(trên)  T05(dưới) T06(trên)  T03(dưới) T04(trên)
```

### 3. Xe Limousine (`LIMOUSINE`)

- **Layout**: 3 cột (2-1 với lối đi)
- **Ghế**: Loại `VIP`
- **Giá**: 200,000 VNĐ
- **Sơ đồ**:

```
L01 L02    L03
L04 L05    L06
...
```

## Xóa xe

### Xóa mềm (Soft Delete)

- Xe được chuyển vào bảng `deleted_buses`
- Lưu trữ thông tin: người xóa, thời gian xóa, lý do xóa
- Có thể khôi phục nếu cần

### Xóa cứng (Hard Delete)

- Xe bị xóa hoàn toàn khỏi database
- Không thể khôi phục
- Chỉ nên dùng khi chắc chắn

## Quy trình đăng ký nhà xe và quyền đăng nhập

### Workflow cập nhật:

1. **User đăng ký nhà xe** → Lưu vào `bus_company_registrations` với status PENDING
2. **Admin duyệt đăng ký** → Tự động tạo `BusCompany` và **tạo User account**
3. **Tạo tài khoản đăng nhập**:
   - Tạo User với role `ROLE_BUS_COMPANY`
   - Link User với BusCompany đã tạo
   - Gửi email thông báo tài khoản và mật khẩu tạm thời
   - User có thể đăng nhập và quản lý xe của nhà xe

### Thông tin tài khoản được tạo:

- **Email**: Email từ đăng ký nhà xe
- **Password**: Mật khẩu tạm thời (gửi qua email)
- **Role**: `ROLE_BUS_COMPANY`
- **BusCompany**: Link đến nhà xe đã được duyệt

## Security

- Tất cả API đều yêu cầu authentication
- User chỉ xem được xe `ACTIVE`
- Bus Company chỉ thao tác được xe của mình
- Admin có quyền cao nhất

## Response Format

```json
{
  "success": true,
  "message": "Thông báo",
  "data": { ... }
}
```

## Error Handling

- `400`: Bad Request (validation error)
- `401`: Unauthorized
- `403`: Forbidden
- `404`: Resource Not Found
- `500`: Internal Server Error
