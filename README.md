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
│   ├── config/          # Cấu hình ứng dụng
│   ├── entity/          # JPA Entities
│   ├── module/
│   │   ├── controller/  # REST Controllers
│   │   ├── service/     # Business Logic
│   │   └── dto/         # Data Transfer Objects
│   ├── repository/      # Data Access Layer
│   └── exception/       # Exception Handling
├── src/main/resources/
│   └── application.yml  # Cấu hình ứng dụng
└── pom.xml             # Maven dependencies
```

## 🗄️ Database Schema

Hệ thống bao gồm các entity chính:

- **User Management**: Users, Roles, UserRole
- **Bus Management**: BusCompany, Bus, BusImage, BusReview, Driver
- **Route & Schedule**: Station, Route, Schedule, BusHistory
- **Booking System**: Seat, Ticket, Payment, PaymentProvider
- **Discount System**: DiscountCode, DiscountUsage
- **Communication**: Conversation, Message
- **Content**: Article, Banner
- **Policy**: CancellationPolicy

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

### Endpoints chính:

- **Welcome**: `GET /api/` - Trang chào mừng với tất cả links
- **Health Check**: `GET /api/health` - Kiểm tra trạng thái ứng dụng
- **Database Health**: `GET /api/health/db` - Kiểm tra kết nối database
- **Swagger UI**: `/api/swagger-ui.html` - Giao diện test API
- **API Docs**: `/api/v3/api-docs` - OpenAPI specification

### Swagger UI

Truy cập http://localhost:8080/api/swagger-ui.html để:

- Xem tất cả API endpoints
- Test API trực tiếp từ browser
- Xem request/response schema
- Tải về OpenAPI specification

## 🔧 Quản lý Database Schema

Sử dụng biến môi trường `DB_DDL_AUTO` để điều khiển việc tạo/cập nhật database:

- `create`: Tạo schema mới, xóa dữ liệu cũ
- `create-drop`: Tạo schema khi start, xóa khi stop
- `update`: Cập nhật schema, giữ dữ liệu (KHUYÊN DÙNG)
- `validate`: Chỉ kiểm tra schema, không thay đổi
- `none`: Không quản lý schema

## 🔐 Security

- Spring Security với JWT authentication
- CORS configuration
- Request validation
- SQL injection protection

## 📝 Logging

- Structured logging với Logback
- Database query logging (có thể bật/tắt)
- Request/Response logging
- Error tracking

## 🧪 Testing

```bash
# Chạy unit tests
mvn test

# Chạy integration tests
mvn verify
```

## 📦 Build Production

```bash
# Build JAR file
mvn clean package

# Chạy JAR file
java -jar target/ck-datveexe-backend-1.0.0.jar
```

## 🤝 Contributing

1. Fork repository
2. Tạo feature branch: `git checkout -b feature/AmazingFeature`
3. Commit changes: `git commit -m 'Add some AmazingFeature'`
4. Push to branch: `git push origin feature/AmazingFeature`
5. Tạo Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 👥 Team

- **Backend Developer**: [Your Name]
- **Database Design**: [Your Name]
- **API Documentation**: [Your Name]

## 📞 Liên hệ

- Email: support@ckdatveexe.com
- GitHub: https://github.com/allecra/BE_ChuyenXeVui

---

⭐ **Star this repo if you find it helpful!**
