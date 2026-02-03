# 🚌 CK DatVeXe - Bus Booking System Backend

## 📋 Mô tả dự án

Backend API cho hệ thống đặt vé xe khách CK DatVeXe, được xây dựng bằng Spring Boot với các tính năng hiện đại.

## 🛠️ Công nghệ sử dụng

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Spring Security**
- **MySQL/TiDB Cloud**
- **Swagger/OpenAPI 3**
- **Lombok**
- **Maven**

## 🏗️ Kiến trúc dự án

```
Backend/
├── src/main/java/com/example/ckdatveexe/
│   ├── config/                    # 🔧 Cấu hình ứng dụng
│   │   ├── SecurityConfig.java    # Spring Security + JWT
│   │   ├── SwaggerConfig.java     # OpenAPI documentation
│   │   ├── DatabaseConfig.java    # Database configuration
│   │   ├── JwtUtils.java          # JWT utilities
│   │   └── UserDetailsImpl.java   # User authentication
│   │
│   ├── module/                    # 📦 Business modules
│   │   ├── auth/                  # 🔐 Authentication
│   │   │   ├── controller/        # Login, register, password reset
│   │   │   ├── service/           # Auth business logic
│   │   │   └── dto/               # Auth request/response DTOs
│   │   │
│   │   ├── bus/                   # 🚌 Bus management
│   │   │   ├── controller/        # User, Company, Admin controllers
│   │   │   ├── service/           # Bus CRUD, seat generation
│   │   │   └── dto/               # Bus DTOs
│   │   │
│   │   ├── seat/                  # 🪑 Seat management
│   │   │   ├── controller/        # User, Company controllers
│   │   │   ├── service/           # Seat CRUD, soft delete
│   │   │   └── dto/               # Seat DTOs
│   │   │
│   │   ├── station/               # 🏢 Station management
│   │   │   ├── controller/        # User, Company controllers
│   │   │   ├── service/           # Station CRUD, bus assignment
│   │   │   └── dto/               # Station DTOs
│   │   │
│   │   ├── buscompany/            # 🏢 Bus company management
│   │   │   ├── controller/        # Registration, Admin, Management
│   │   │   ├── service/           # Company business logic
│   │   │   └── dto/               # Company DTOs
│   │   │
│   │   └── media/                 # 📷 File upload (Cloudinary)
│   │       ├── controller/
│   │       └── service/
│   │
│   ├── shared/                    # 🔄 Shared components
│   │   ├── entity/                # JPA Entities
│   │   ├── repository/            # Data Access Layer
│   │   ├── dto/                   # Common DTOs
│   │   └── guard/                 # Authorization guards
│   │
│   ├── exception/                 # ⚠️ Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   │
│   └── CkDatVeXeApplication.java  # 🚀 Main application
│
├── src/main/resources/
│   ├── db/migration/              # 🗄️ Flyway migrations
│   │   ├── V1__Create_bus_company_registration_table.sql
│   │   ├── V2__Create_bus_and_seat_tables.sql
│   │   ├── V3__Update_seat_type_enum.sql
│   │   ├── V4__Add_bus_company_role.sql
│   │   ├── V5__Update_phone_column_length.sql
│   │   ├── V6__Fix_seat_type_enum.sql
│   │   └── V7__Update_seat_status_enum.sql
│   │
│   └── application.yml            # 📝 Application configuration
│
├── src/test/java/                 # 🧪 Test files
├── target/                        # 📦 Build output
├── .env.example                   # 🔧 Environment template
├── .env                          # 🔐 Environment variables (gitignored)
├── pom.xml                       # 📋 Maven dependencies
└── README.md                     # 📖 Documentation
```

### 🎯 Module Architecture

#### 🔐 Authentication Module

- JWT-based authentication
- Role-based authorization
- Password reset with OTP
- Refresh token mechanism

#### 🚌 Bus Module

- Multi-role access (USER, BUS_COMPANY, ADMIN)
- Soft/hard delete functionality
- Automatic seat generation
- Image upload integration

#### 🪑 Seat Module

- Role-based CRUD operations
- Soft delete with business rules
- Advanced search and filtering
- Price and status management

#### 🏢 Station Module

- Multi-role access (USER views only, BUS_COMPANY full CRUD)
- Bus-Station many-to-many relationships
- Soft/hard delete functionality
- Advanced search and bus assignment

#### 🏢 Bus Company Module

- Public registration workflow
- Admin approval system
- Self-management features
- Email notifications

## 🗄️ Database Schema

### 📋 Core Entities

#### 👥 User Management

- **users**: Thông tin người dùng (USER, BUS_COMPANY, ADMIN)
- **roles**: Vai trò hệ thống
- **password_resets**: Quản lý reset mật khẩu
- **refresh_tokens**: JWT refresh tokens

#### 🏢 Bus Company System

- **bus_companies**: Thông tin nhà xe
- **bus_company_registrations**: Đăng ký nhà xe chờ duyệt
- **drivers**: Tài xế

#### 🚌 Bus & Seat Management

- **buses**: Thông tin xe khách
- **bus_images**: Hình ảnh xe
- **bus_reviews**: Đánh giá xe
- **bus_history**: Lịch sử hoạt động xe
- **deleted_buses**: Xe đã xóa mềm
- **seats**: Ghế ngồi với layout và giá

#### 🛣️ Route & Schedule

- **stations**: Bến xe, trạm dừng
- **routes**: Tuyến đường
- **schedules**: Lịch trình chạy xe

#### 🎫 Booking System

- **tickets**: Vé đặt
- **payments**: Thanh toán
- **payment_providers**: Nhà cung cấp thanh toán

#### 🎁 Discount System

- **discount_codes**: Mã giảm giá
- **discount_usage**: Lịch sử sử dụng mã

#### 💬 Communication

- **conversations**: Cuộc trò chuyện
- **messages**: Tin nhắn

#### 📰 Content Management

- **articles**: Bài viết
- **banners**: Banner quảng cáo
- **cancellation_policies**: Chính sách hủy vé

### 🔄 Database Migrations

Hệ thống sử dụng Flyway migrations:

- `V1__Create_bus_company_registration_table.sql` - Tạo bảng đăng ký nhà xe
- `V2__Create_bus_and_seat_tables.sql` - Tạo bảng xe và ghế
- `V3__Update_seat_type_enum.sql` - Cập nhật enum loại ghế
- `V4__Add_bus_company_role.sql` - Thêm role BUS_COMPANY
- `V5__Update_phone_column_length.sql` - Tăng độ dài cột phone
- `V6__Fix_seat_type_enum.sql` - Sửa enum SeatType
- `V7__Update_seat_status_enum.sql` - Thêm MAINTENANCE, DELETED cho SeatStatus

### 📊 Key Enums

#### SeatStatus

- `AVAILABLE` - Ghế trống, có thể đặt
- `BOOKED` - Ghế đã được đặt
- `MAINTENANCE` - Ghế đang bảo trì
- `DELETED` - Ghế đã bị xóa mềm

#### SeatType

- `STANDARD` - Ghế thường
- `VIP` - Ghế VIP
- `SLEEPER` - Ghế nằm
- `BUSINESS` - Ghế thương gia

#### BusStatus

- `ACTIVE` - Xe đang hoạt động
- `MAINTENANCE` - Xe đang bảo trì
- `INACTIVE` - Xe ngừng hoạt động

#### UserStatus

- `ACTIVE` - Tài khoản hoạt động
- `BLOCKED` - Tài khoản bị khóa
- `PENDING` - Tài khoản chờ kích hoạt

### 🔗 Key Relationships

- **User** ↔ **BusCompany**: One-to-One (nhà xe có 1 tài khoản)
- **BusCompany** ↔ **Bus**: One-to-Many (nhà xe có nhiều xe)
- **Bus** ↔ **Seat**: One-to-Many (xe có nhiều ghế)
- **User** ↔ **Ticket**: One-to-Many (user đặt nhiều vé)
- **Seat** ↔ **Ticket**: One-to-One (1 ghế = 1 vé)

## ⚙️ Cấu hình môi trường

### 1. Tạo file `.env` từ `.env.example`:

```bash
cp Backend/.env.example Backend/.env
```

### 2. Cấu hình database trong `.env`:

```env
# Database Configuration
DB_HOST=your-database-host
DB_PORT=4000
DB_NAME=your-database-name
DB_USERNAME=your-username
DB_PASSWORD=your-password

# Database Schema Management
DB_DDL_AUTO=update  # create, create-drop, update, validate, none
DB_SHOW_SQL=true

# Server Configuration
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api
```

## 🚀 Chạy ứng dụng

### Yêu cầu hệ thống:

- Java 17+
- Maven 3.6+
- MySQL 8.0+ hoặc TiDB Cloud

### Các bước chạy:

1. **Clone repository:**

   ```bash
   git clone https://github.com/allecra/BE_ChuyenXeVui.git
   cd BE_ChuyenXeVui
   ```

2. **Cấu hình database:**

   ```bash
   cp Backend/.env.example Backend/.env
   # Chỉnh sửa thông tin database trong .env
   ```

3. **Build và chạy:**

   ```bash
   cd Backend
   mvn clean compile
   mvn spring-boot:run
   ```

4. **Truy cập ứng dụng:**
   - API Base: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/api/swagger-ui.html
   - Health Check: http://localhost:8080/api/health

## 📚 API Documentation

### 🔗 System Endpoints

- **Welcome**: `GET /api/` - Trang chào mừng với tất cả links
- **Health Check**: `GET /api/health` - Kiểm tra trạng thái ứng dụng
- **Database Health**: `GET /api/health/db` - Kiểm tra kết nối database
- **Swagger UI**: `/api/swagger-ui.html` - Giao diện test API
- **API Docs**: `/api/v3/api-docs` - OpenAPI specification

### 🔐 Authentication API (`/api/auth`)

**Public Endpoints (không cần đăng nhập):**

- `POST /register` - Đăng ký tài khoản người dùng
- `POST /login` - Đăng nhập
- `POST /refresh-token` - Làm mới JWT token
- `POST /forgot-password` - Quên mật khẩu
- `POST /verify-otp` - Xác thực OTP
- `POST /reset-password` - Đặt lại mật khẩu

### 🚌 Bus API

#### 👤 User Bus API (`/api/user/buses`)

- `GET /` - Tìm kiếm xe (có phân trang, lọc)
- `GET /{busId}` - Xem chi tiết xe
- `GET /company/{companyId}` - Xem xe của nhà xe

#### 🏢 Bus Company API (`/api/bus-company/buses`)

**Yêu cầu role: `BUS_COMPANY`**

- `POST /` - Tạo xe mới
- `GET /` - Danh sách xe của nhà xe
- `GET /{busId}` - Chi tiết xe
- `PUT /{busId}` - Cập nhật thông tin xe
- `DELETE /{busId}` - Xóa mềm xe
- `POST /{busId}/restore` - Khôi phục xe đã xóa
- `PUT /{busId}/layout` - Cập nhật layout ghế

#### 👑 Admin Bus API (`/api/admin/buses`)

**Yêu cầu role: `ADMIN`**

- `GET /` - Tất cả xe trong hệ thống
- `GET /{busId}` - Chi tiết xe
- `PUT /{busId}` - Cập nhật xe
- `DELETE /{busId}` - Xóa cứng xe
- `GET /deleted` - Danh sách xe đã xóa

### 🪑 Seat API

#### 👤 User Seat API (`/api/user/seats`)

**Chỉ xem ghế không bị xóa**

- `GET /bus/{busId}` - Danh sách ghế của xe
- `GET /{seatId}` - Chi tiết ghế
- `GET /available/bus/{busId}` - Ghế trống có thể đặt

#### 🏢 Bus Company Seat API (`/api/bus-company/seats`)

**Yêu cầu role: `BUS_COMPANY`**

- `POST /` - Tạo ghế mới
- `GET /` - Danh sách tất cả ghế (bao gồm đã xóa)
- `GET /{seatId}` - Chi tiết ghế
- `PUT /{seatId}` - Cập nhật thông tin ghế
- `PUT /{seatId}/status` - Cập nhật trạng thái ghế
- `PUT /{seatId}/price` - Cập nhật giá ghế
- `DELETE /{seatId}` - Xóa mềm ghế

### 🏢 Station API

#### � User Station API (`/api/user/stations`)

**Chỉ xem bến xe và xe đang hoạt động**

- `GET /` - Danh sách tất cả bến xe (có phân trang, lọc)
- `GET /{stationId}` - Chi tiết bến xe
- `GET /{stationId}/buses` - Danh sách xe tại bến (chỉ xe ACTIVE)
- `GET /search` - Tìm kiếm bến xe theo từ khóa
- `GET /{stationId}/buses/search` - Tìm kiếm xe trong bến

#### 🏢 Bus Company Station API (`/api/bus-company/stations`)

**Yêu cầu role: `BUS_COMPANY`**

- `POST /` - Tạo bến xe mới
- `GET /` - Danh sách tất cả bến xe (full details)
- `GET /{stationId}` - Chi tiết bến xe
- `PUT /{stationId}` - Cập nhật thông tin bến xe
- `POST /assign-buses` - Gắn xe vào bến
- `DELETE /{stationId}/buses` - Gỡ xe khỏi bến
- `GET /{stationId}/buses` - Danh sách xe tại bến (tất cả trạng thái)
- `GET /search` - Tìm kiếm bến xe
- `GET /{stationId}/buses/search` - Tìm kiếm xe trong bến
- `DELETE /{stationId}` - Xóa bến xe (soft/hard delete)

### 🏢 Bus Company Management API

#### 📝 Registration (`/api/bus-company/registration`)

**Public Endpoint:**

- `POST /register` - Đăng ký nhà xe (không cần đăng nhập)

#### 👑 Admin Management (`/api/admin/bus-companies`)

**Yêu cầu role: `ADMIN`**

- `GET /registrations` - Danh sách đăng ký chờ duyệt
- `POST /registrations/{id}/approve` - Duyệt đăng ký
- `POST /registrations/{id}/reject` - Từ chối đăng ký
- `GET /` - Tất cả nhà xe
- `PUT /{companyId}` - Cập nhật thông tin nhà xe
- `GET /search` - Tìm kiếm nhà xe
- `GET /{companyId}/buses/{busId}` - Chi tiết xe của nhà xe
- `DELETE /{companyId}/buses/{busId}` - Xóa xe
- `POST /{companyId}/restore` - Khôi phục tài khoản

#### 🏢 Company Self-Management (`/api/bus-company/management`)

**Yêu cầu role: `BUS_COMPANY`**

- `PUT /profile` - Cập nhật thông tin nhà xe
- `GET /buses/search` - Tìm kiếm xe của mình
- `PUT /change-password` - Đổi mật khẩu
- `POST /reset-password` - Đặt lại mật khẩu

### 🎯 API Features

#### 🔒 Authentication & Authorization

- **JWT Token**: Tất cả API (trừ public) yêu cầu Bearer token
- **Role-based Access**: 3 roles chính
  - `USER`: Người dùng thường
  - `BUS_COMPANY`: Nhà xe
  - `ADMIN`: Quản trị viên

#### 📊 Response Format

```json
{
  "success": true,
  "message": "Thông báo",
  "data": {
    /* Dữ liệu response */
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

#### 🔍 Search & Pagination

- **Pagination**: `page`, `size`, `sortBy`, `sortDirection`
- **Filtering**: Theo status, type, price range, etc.
- **Search**: Tìm kiếm theo tên, số ghế, etc.

#### 📝 Status Codes & Logging

- **200 OK** ✅ - Thành công
- **201 CREATED** 🆕 - Tạo mới thành công
- **400 BAD REQUEST** ❌ - Dữ liệu không hợp lệ
- **401 UNAUTHORIZED** 🚫 - Chưa đăng nhập
- **403 FORBIDDEN** 🔒 - Không có quyền
- **404 NOT FOUND** 🔍 - Không tìm thấy
- **500 INTERNAL ERROR** 💥 - Lỗi hệ thống

### Swagger UI

Truy cập http://localhost:8080/api/swagger-ui.html để:

- Xem tất cả API endpoints với role requirements
- Test API trực tiếp từ browser
- Xem request/response schema chi tiết
- Tải về OpenAPI specification
- Thử nghiệm authentication với JWT token

## 🔧 Quản lý Database Schema

Sử dụng biến môi trường `DB_DDL_AUTO` để điều khiển việc tạo/cập nhật database:

- `create`: Tạo schema mới, xóa dữ liệu cũ
- `create-drop`: Tạo schema khi start, xóa khi stop
- `update`: Cập nhật schema, giữ dữ liệu (KHUYÊN DÙNG)
- `validate`: Chỉ kiểm tra schema, không thay đổi
- `none`: Không quản lý schema

## 🔐 Security & Business Rules

### 🛡️ Authentication & Authorization

#### JWT Token System

- **Access Token**: Thời hạn ngắn (15 phút)
- **Refresh Token**: Thời hạn dài (7 ngày)
- **Token Format**: `Bearer <jwt-token>`

#### Role-based Access Control

- **ROLE_USER**: Người dùng thường - xem thông tin, đặt vé
- **ROLE_BUS_COMPANY**: Nhà xe - quản lý xe và ghế của mình
- **ROLE_ADMIN**: Quản trị viên - toàn quyền hệ thống

#### Security Features

- CORS configuration cho frontend
- SQL injection protection
- Request validation với Bean Validation
- Password encryption với BCrypt
- Rate limiting (có thể cấu hình)

### 📋 Business Rules

#### 🚌 Bus Management

- Nhà xe chỉ quản lý xe của mình
- Xe có 3 trạng thái: ACTIVE, MAINTENANCE, INACTIVE
- Xóa mềm: xe chuyển sang bảng `deleted_buses`
- Khôi phục xe: chuyển từ `deleted_buses` về `buses`

#### 🪑 Seat Management

- **USER**: Chỉ xem ghế (loại trừ DELETED)
- **BUS_COMPANY**: Toàn quyền CRUD ghế của xe mình
- **Soft Delete**: Ghế DELETED không hiển thị cho USER
- **Business Rules**:
  - Không thể xóa ghế BOOKED
  - Không thể cập nhật ghế BOOKED
  - Ghế DELETED không tham gia đặt vé
  - Số ghế phải unique trong 1 xe

#### 🏢 Bus Company Registration

- **Public Registration**: Không cần đăng nhập
- **Admin Approval**: Admin duyệt/từ chối đăng ký
- **Auto Account Creation**: Tạo User với ROLE_BUS_COMPANY khi duyệt
- **Email Notification**: Gửi email thông báo kết quả

#### 🔄 Status Transitions

- **Seat Status**: AVAILABLE ↔ BOOKED ↔ MAINTENANCE
- **DELETED**: Không thể chuyển sang trạng thái khác
- **BOOKED**: Không thể xóa hoặc cập nhật thông tin

### 🚨 Error Handling

#### Global Exception Handler

- **ResourceNotFoundException** → 404 NOT_FOUND
- **IllegalArgumentException** → 400 BAD_REQUEST
- **AccessDeniedException** → 403 FORBIDDEN
- **AuthenticationException** → 401 UNAUTHORIZED
- **ValidationException** → 400 BAD_REQUEST
- **General Exception** → 500 INTERNAL_SERVER_ERROR

#### Comprehensive Logging

- **Request/Response**: Tất cả API calls
- **Authentication**: Login attempts, token validation
- **Business Operations**: CRUD operations với user info
- **Error Tracking**: Stack traces cho debugging
- **Status Code Logging**: Với emoji indicators

### 📧 Email System

#### Email Templates

- **Registration Approval**: Thông báo duyệt nhà xe
- **Registration Rejection**: Thông báo từ chối
- **Password Reset**: OTP và link reset
- **Company Info Update**: Thông báo thay đổi thông tin

#### Email Configuration

- SMTP server configuration
- Template engine support
- Async email sending
- Retry mechanism

## 📝 Logging

- Structured logging với Logback
- Database query logging (có thể bật/tắt)
- Request/Response logging
- Error tracking

## 🧪 Testing & Development

### 🔧 Development Setup

```bash
# Clone repository
git clone https://github.com/allecra/BE_ChuyenXeVui.git
cd BE_ChuyenXeVui/Backend

# Setup environment
cp .env.example .env
# Edit .env with your database credentials

# Install dependencies & compile
mvn clean compile

# Run in development mode
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run specific test class
mvn test -Dtest=SeatServiceTest

# Run with coverage
mvn test jacoco:report
```

### 📊 Code Quality

```bash
# Check code style
mvn checkstyle:check

# Static analysis
mvn spotbugs:check

# Dependency check
mvn dependency-check:check
```

## 🚀 Deployment

### 📦 Build Production

```bash
# Build JAR file
mvn clean package -DskipTests

# Build with tests
mvn clean package

# The JAR file will be in target/
ls target/*.jar
```

### 🐳 Docker Deployment

```dockerfile
# Dockerfile example
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build Docker image
docker build -t ckdatveexe-backend .

# Run container
docker run -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e DB_USERNAME=your-username \
  -e DB_PASSWORD=your-password \
  ckdatveexe-backend
```

### ☁️ Cloud Deployment

#### Environment Variables

```env
# Production settings
SPRING_PROFILES_ACTIVE=prod
DB_HOST=production-db-host
DB_USERNAME=prod-user
DB_PASSWORD=secure-password
JWT_SECRET=your-jwt-secret-key
EMAIL_HOST=smtp.gmail.com
EMAIL_USERNAME=your-email
EMAIL_PASSWORD=your-app-password
```

#### Health Checks

- **Liveness**: `GET /api/health`
- **Readiness**: `GET /api/health/db`
- **Metrics**: `GET /api/actuator/metrics` (if enabled)

### 🔧 Configuration Management

#### Application Profiles

- **default**: Development với H2 database
- **dev**: Development với MySQL
- **test**: Testing với test database
- **prod**: Production với optimized settings

#### Database Migration

```bash
# Check migration status
mvn flyway:info

# Migrate to latest
mvn flyway:migrate

# Rollback (if needed)
mvn flyway:undo
```

## 🔧 Troubleshooting

### 🚨 Common Issues

#### Database Connection Issues

```bash
# Check database connectivity
curl http://localhost:8080/api/health/db

# Common solutions:
# 1. Verify .env database credentials
# 2. Check if database server is running
# 3. Verify firewall settings
# 4. Check connection string format
```

#### Authentication Issues

```bash
# Test authentication endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'

# Common solutions:
# 1. Check JWT secret configuration
# 2. Verify user exists in database
# 3. Check password encoding
# 4. Validate token expiration
```

#### API Access Issues

```bash
# Test with JWT token
curl -X GET http://localhost:8080/api/user/buses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Common solutions:
# 1. Include Bearer prefix in Authorization header
# 2. Check token expiration
# 3. Verify user role permissions
# 4. Check endpoint URL spelling
```

### 📊 Monitoring & Logs

#### Application Logs

```bash
# View application logs
tail -f logs/application.log

# Filter by log level
grep "ERROR" logs/application.log
grep "🚫\|❌\|💥" logs/application.log  # Filter by emoji status codes
```

#### Database Logs

```bash
# Enable SQL logging in application.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

#### Performance Monitoring

- Check `/api/health` for system status
- Monitor database connection pool
- Track JWT token generation/validation
- Monitor API response times

### 🔍 Debug Mode

```bash
# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dlogging.level.com.example.ckdatveexe=DEBUG"

# Enable SQL parameter logging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dlogging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
```

## 🤝 Contributing

### 📋 Development Guidelines

1. **Code Style**: Follow Java conventions và Spring Boot best practices
2. **Testing**: Viết unit tests cho business logic
3. **Documentation**: Cập nhật API documentation trong Swagger
4. **Security**: Luôn validate input và check authorization
5. **Logging**: Sử dụng structured logging với emoji status codes

### 🔄 Git Workflow

```bash
# 1. Fork repository
git clone https://github.com/your-username/BE_ChuyenXeVui.git

# 2. Create feature branch
git checkout -b feature/seat-booking-system

# 3. Make changes and commit
git add .
git commit -m "feat: add seat booking functionality"

# 4. Push and create PR
git push origin feature/seat-booking-system
# Create Pull Request on GitHub
```

### 📝 Commit Convention

- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation updates
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `test:` - Test additions/updates
- `chore:` - Build/config changes

## 👥 Team & Contact

### 🏆 Development Team

- **Lead Backend Developer**: Responsible for API design và database architecture
- **Security Engineer**: JWT authentication và authorization system
- **Database Administrator**: Schema design và migration management
- **DevOps Engineer**: Deployment và monitoring setup

### 📞 Support & Contact

- **Technical Support**: support@ckdatveexe.com
- **Bug Reports**: [GitHub Issues](https://github.com/allecra/BE_ChuyenXeVui/issues)
- **Feature Requests**: [GitHub Discussions](https://github.com/allecra/BE_ChuyenXeVui/discussions)
- **Documentation**: [API Docs](http://localhost:8080/api/swagger-ui.html)

### 🌟 Acknowledgments

- Spring Boot community for excellent framework
- JWT.io for authentication standards
- Swagger/OpenAPI for API documentation
- MySQL/TiDB for reliable database solutions

---

## 📈 Project Status

- ✅ **Authentication System**: Complete với JWT và role-based access
- ✅ **Bus Management**: Full CRUD với multi-role support
- ✅ **Seat Management**: Complete với soft delete và business rules
- ✅ **Station Management**: Complete với bus assignment và multi-role access
- ✅ **Bus Company System**: Registration workflow và management
- ✅ **API Documentation**: Comprehensive Swagger documentation
- ✅ **Database Migrations**: Flyway migrations với version control
- 🔄 **Booking System**: In development
- 🔄 **Payment Integration**: Planned
- 🔄 **Notification System**: Planned

**Current Version**: 1.0.0  
**Last Updated**: January 2025  
**API Endpoints**: 35+ endpoints across 5 modules  
**Database Tables**: 15+ entities với relationships

⭐ **Star this repo if you find it helpful!**
