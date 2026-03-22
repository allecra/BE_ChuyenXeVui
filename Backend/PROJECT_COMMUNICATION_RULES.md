# 📋 Quy Tắc Giao Tiếp và Phát Triển Dự Án

## 🎯 Thông Tin Dự Án

- **Tên dự án**: Hệ thống đặt vé xe khách CK_DatVeXe
- **Công nghệ**: Spring Boot 3.x, Java 17, PostgreSQL, Redis
- **Kiến trúc**: RESTful API, Microservices-ready
- **Phiên bản hiện tại**: 1.0.0

---

## 🤝 Quy Tắc Giao Tiếp

### 1. 📝 Ngôn Ngữ Sử Dụng

- **Giao tiếp**: Tiếng Việt
- **Code & Comments**: Tiếng Anh
- **API Documentation**: Tiếng Việt (description), Tiếng Anh (technical)
- **Commit Messages**: Tiếng Việt với emoji

### 2. 💬 Phong Cách Giao Tiếp

- **Tôn trọng**: Luôn lịch sự và chuyên nghiệp
- **Rõ ràng**: Mô tả yêu cầu cụ thể, tránh mơ hồ
- **Hiệu quả**: Tập trung vào giải pháp, không lan man
- **Phản hồi**: Xác nhận hiểu yêu cầu trước khi thực hiện

### 3. 🔄 Quy Trình Làm Việc

1. **Nhận yêu cầu** → Xác nhận hiểu đúng
2. **Phân tích** → Đưa ra giải pháp kỹ thuật
3. **Thực hiện** → Code và test
4. **Báo cáo** → Tóm tắt những gì đã làm
5. **Cập nhật tài liệu** → Ghi chú vào API_SYSTEM_DOCUMENTATION.md

---

## 🛠️ Quy Tắc Phát Triển

### 1. 🏗️ Kiến Trúc Code

```
Backend/
├── src/main/java/com/example/ckdatveexe/
│   ├── config/           # Cấu hình hệ thống
│   ├── exception/        # Xử lý lỗi global
│   ├── shared/          # Entity, Repository, Util chung
│   │   ├── entity/      # JPA Entities
│   │   ├── repository/  # JPA Repositories
│   │   └── util/        # Utility classes
│   └── module/          # Các module chức năng
│       ├── auth/        # Authentication & Authorization
│       ├── user/        # User Management
│       ├── bus/         # Bus Management
│       ├── route/       # Route Management
│       ├── schedule/    # Schedule Management
│       ├── ticket/      # Ticket Booking
│       ├── payment/     # Payment System
│       ├── discount/    # Discount System
│       ├── review/      # Review System
│       ├── driver/      # Driver Management
│       └── station/     # Station Management
```

### 2. 🎯 Phân Quyền Hệ Thống

- **USER**: Khách hàng đặt vé
  - Xem lịch trình, đặt vé, thanh toán
  - Quản lý profile, lịch sử đặt vé
  - Đánh giá dịch vụ
- **COMPANY**: Nhà xe
  - Quản lý xe, lịch trình, tài xế
  - Xem báo cáo doanh thu
  - Tạo mã giảm giá
- **ADMIN**: Quản trị viên
  - Quản lý toàn bộ hệ thống
  - Duyệt nhà xe, bến xe
  - Quản lý người dùng

### 3. 📊 Chuẩn API Response

```json
// Success Response
{
  "success": true,
  "message": "Thành công",
  "data": {...},
  "timestamp": "2026-03-22T10:30:00"
}

// Error Response
{
  "success": false,
  "message": "Lỗi xảy ra",
  "error": "Chi tiết lỗi",
  "timestamp": "2026-03-22T10:30:00"
}

// Pagination Response
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  }
}
```

### 4. 🔒 Bảo Mật

- **JWT Authentication**: Access token (24h), Refresh token (7 days)
- **Role-based Authorization**: @PreAuthorize annotations
- **Input Validation**: @Valid, @Validated
- **SQL Injection Prevention**: JPA Queries, Parameterized queries
- **Rate Limiting**: Giới hạn request per IP

---

## 📝 Quy Tắc Commit

### 1. 🎨 Format Commit Message

```
🎯 [Type] Tiêu đề ngắn gọn

✨ Tính năng mới:
- Feature 1
- Feature 2

🔧 Sửa lỗi:
- Bug fix 1
- Bug fix 2

📚 Tài liệu:
- Documentation updates

🎯 Phân quyền:
- Role-based changes
```

### 2. 🏷️ Emoji Categories

- 🚀 **Major Feature**: Tính năng lớn
- ✨ **Feature**: Tính năng mới
- 🔧 **Fix**: Sửa lỗi
- 📚 **Docs**: Tài liệu
- 🎯 **Auth**: Phân quyền
- 💾 **Database**: Thay đổi DB
- 🔒 **Security**: Bảo mật
- ⚡ **Performance**: Tối ưu hiệu suất
- 🧪 **Test**: Testing
- 🎨 **Style**: Code formatting

### 3. 🌿 Git Branching Strategy

- **master**: Production-ready code
- **Feature/[APIName]**: Tính năng mới
- **Fix/[APIName]**: Sửa lỗi
- **Update/[APIName]**: Cập nhật tính năng
- **Feature_Update/[APIName]**: Cập nhật lớn
- **Feature_Fix/[APIName]**: Sửa lỗi trong feature

---

## 🎯 Nguyên Tắc Làm Việc

### 1. ✅ Khi Nhận Yêu Cầu Mới

1. **Đọc kỹ yêu cầu** và hỏi lại nếu không rõ
2. **Phân tích tác động** đến hệ thống hiện tại
3. **Đề xuất giải pháp** kỹ thuật
4. **Ước tính thời gian** thực hiện
5. **Xác nhận** với người yêu cầu trước khi bắt đầu

### 2. 🔄 Trong Quá Trình Phát Triển

1. **Code theo chuẩn** đã định
2. **Test thoroughly** trước khi commit
3. **Update documentation** ngay lập tức
4. **Commit với message rõ ràng**
5. **Báo cáo tiến độ** định kỳ

### 3. 📋 Khi Hoàn Thành

1. **Tóm tắt những gì đã làm**
2. **Cập nhật API_SYSTEM_DOCUMENTATION.md**
3. **Ghi chú breaking changes** (nếu có)
4. **Đề xuất testing scenarios**
5. **Sẵn sàng support** nếu có vấn đề

### 4. 🚨 Khi Gặp Vấn Đề

1. **Báo cáo ngay lập tức** với chi tiết lỗi
2. **Đề xuất giải pháp** thay thế
3. **Không tự ý thay đổi** requirement
4. **Hỏi ý kiến** trước khi quyết định lớn

---

## 📞 Thông Tin Liên Hệ

### 🔧 Technical Support

- **Database Issues**: Kiểm tra connection, migration scripts
- **API Errors**: Xem logs, validate input/output
- **Performance**: Profiling, query optimization
- **Security**: Authentication, authorization issues

### 📚 Documentation Updates

- **API Changes**: Cập nhật endpoints, parameters
- **Database Changes**: Migration scripts, entity relationships
- **Business Logic**: Workflow, validation rules
- **Configuration**: Environment variables, properties

---

## 🎯 Mục Tiêu Chất Lượng

### 1. 📊 Code Quality

- **Clean Code**: Readable, maintainable
- **SOLID Principles**: Single responsibility, Open/closed, etc.
- **Design Patterns**: Repository, Service, Factory patterns
- **Error Handling**: Comprehensive exception handling

### 2. 🚀 Performance

- **Database**: Optimized queries, proper indexing
- **Caching**: Redis for frequently accessed data
- **API Response Time**: < 500ms for most endpoints
- **Concurrent Users**: Support 1000+ simultaneous users

### 3. 🔒 Security

- **Authentication**: JWT with proper expiration
- **Authorization**: Role-based access control
- **Data Protection**: Encryption, input validation
- **Audit Trail**: Logging all critical operations

---

_Tài liệu này sẽ được cập nhật thường xuyên theo sự phát triển của dự án._
