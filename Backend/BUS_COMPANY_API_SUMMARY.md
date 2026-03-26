# Tóm tắt API Quản lý Nhà Xe - Bus Company API Summary

## 🚀 Đã hoàn thành và sửa lỗi

### ✅ Vấn đề đã được giải quyết:

1. **401 Unauthorized Error**: Đã sửa cấu hình security để cho phép truy cập public endpoints
2. **Email không gửi được**: Đã cấu hình SMTP và tạo các email templates
3. **Trả về email trong response**: Đã cập nhật DTO để trả về đầy đủ thông tin
4. **Context path conflict**: Đã loại bỏ context path để tránh double `/api`

## 📋 Danh sách API đầy đủ

### 🌐 Public APIs (Không cần đăng nhập)

#### 1. Đăng ký nhà xe

- **POST** `/api/public/bus-company/register`
- **Mô tả**: Đăng ký nhà xe mới
- **Body**: CompanyName, Email, PhoneNumber, Image, Descriptions, BusinessLicense, Address
- **Response**: Thông tin đăng ký + email xác nhận

#### 2. Kiểm tra trạng thái đăng ký

- **GET** `/api/public/bus-company/registration-status/{email}`
- **Mô tả**: Kiểm tra trạng thái đăng ký bằng email
- **Response**: PENDING/APPROVED/REJECTED + thông báo

#### 3. Test endpoint

- **GET** `/api/public/bus-company/test`
- **Mô tả**: Endpoint test để kiểm tra API hoạt động

### 👑 Admin APIs (Cần quyền ADMIN)

#### 4. Danh sách đăng ký nhà xe

- **GET** `/api/admin/bus-company/registrations`
- **Params**: status, companyName, page, size, sortBy, sortDirection
- **Mô tả**: Lấy danh sách tất cả đăng ký nhà xe

#### 5. Tìm kiếm đăng ký

- **GET** `/api/admin/bus-company/registrations/search`
- **Params**: searchTerm, status, page, size, sortBy, sortDirection
- **Mô tả**: Tìm kiếm đăng ký theo từ khóa

#### 6. Chi tiết đăng ký

- **GET** `/api/admin/bus-company/registrations/{registrationId}`
- **Mô tả**: Xem chi tiết một đăng ký cụ thể

#### 7. Duyệt đăng ký

- **POST** `/api/admin/bus-company/registrations/{registrationId}/approve`
- **Body**: admin_notes (optional)
- **Mô tả**: Duyệt đăng ký → Tạo tài khoản + gửi email

#### 8. Từ chối đăng ký

- **POST** `/api/admin/bus-company/registrations/{registrationId}/reject`
- **Body**: reason (required)
- **Mô tả**: Từ chối đăng ký + gửi email thông báo lý do

#### 9. Danh sách nhà xe đã duyệt

- **GET** `/api/admin/bus-company`
- **Params**: keyword, page, size, sortBy, sortDirection
- **Mô tả**: Lấy danh sách tất cả nhà xe đã được duyệt

#### 10. Tìm kiếm nhà xe

- **GET** `/api/admin/bus-company/search`
- **Params**: keyword, page, size, sortBy, sortDirection
- **Mô tả**: Tìm kiếm nhà xe đã duyệt

#### 11. Cập nhật thông tin nhà xe

- **PUT** `/api/admin/bus-company/{companyId}`
- **Body**: BusCompanyUpdateRequest
- **Mô tả**: Cập nhật thông tin nhà xe + gửi email thông báo

#### 12. Reset mật khẩu nhà xe

- **POST** `/api/admin/bus-company/reset-password`
- **Params**: email
- **Mô tả**: Reset mật khẩu cho tài khoản nhà xe

#### 13. Xem nhà xe sở hữu xe

- **GET** `/api/admin/bus-company/bus-owner`
- **Params**: busId
- **Mô tả**: Xem xe thuộc về nhà xe nào

#### 14. Chi tiết xe của nhà xe

- **GET** `/api/admin/bus-company/{companyId}/buses/{busId}`
- **Mô tả**: Xem chi tiết xe của một nhà xe cụ thể

#### 15. Xóa xe của nhà xe

- **DELETE** `/api/admin/bus-company/{companyId}/buses/{busId}`
- **Body**: DeleteBusRequest (hard_delete: boolean)
- **Mô tả**: Xóa xe (cứng hoặc mềm)

#### 16. Khôi phục tài khoản nhà xe

- **POST** `/api/admin/bus-company/{companyId}/restore-account`
- **Mô tả**: Khôi phục tài khoản nhà xe đã bị khóa

#### 17. Xóa nhà xe

- **DELETE** `/api/admin/bus-company/{companyId}`
- **Body**: DeleteBusCompanyRequest (hard_delete: boolean)
- **Mô tả**: Xóa nhà xe (cứng hoặc mềm)

## 🔄 Workflow hoàn chỉnh

### 1. Đăng ký nhà xe

```
Nhà xe → POST /api/public/bus-company/register
       → Nhận email xác nhận
       → Status: PENDING
```

### 2. Admin duyệt

```
Admin → GET /api/admin/bus-company/registrations?status=PENDING
      → GET /api/admin/bus-company/registrations/{id}
      → POST /api/admin/bus-company/registrations/{id}/approve
      → Tạo BusCompany + User account
      → Gửi email với thông tin đăng nhập
      → Status: APPROVED
```

### 3. Admin từ chối

```
Admin → POST /api/admin/bus-company/registrations/{id}/reject
      → Gửi email thông báo lý do
      → Status: REJECTED
```

### 4. Nhà xe kiểm tra trạng thái

```
Nhà xe → GET /api/public/bus-company/registration-status/{email}
       → Nhận thông báo trạng thái hiện tại
```

## 📧 Email Templates

### 1. Email xác nhận đăng ký

- **Trigger**: Khi đăng ký thành công
- **Content**: Xác nhận đã nhận đăng ký, đang xem xét

### 2. Email thông báo duyệt

- **Trigger**: Khi admin approve
- **Content**: Thông tin đăng nhập (email + mật khẩu tạm thời)

### 3. Email thông báo từ chối

- **Trigger**: Khi admin reject
- **Content**: Lý do từ chối + hướng dẫn đăng ký lại

### 4. Email reset mật khẩu

- **Trigger**: Khi admin reset password
- **Content**: Mật khẩu mới

### 5. Email cập nhật thông tin

- **Trigger**: Khi admin cập nhật thông tin nhà xe
- **Content**: Thông báo thay đổi

## 🔐 Security & Authorization

### Public Endpoints (Không cần token)

- `/api/public/bus-company/**`

### Admin Endpoints (Cần ROLE_ADMIN)

- `/api/admin/bus-company/**`

### Authentication Flow

```
1. Admin đăng nhập → Nhận JWT token
2. Sử dụng token trong header: Authorization: Bearer <token>
3. Truy cập các API admin
```

## 🧪 Testing

### 1. Test đăng ký nhà xe

```bash
curl -X POST http://localhost:8080/api/public/bus-company/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Nhà xe Test",
    "email": "test@example.com",
    "phoneNumber": "0123456789"
  }'
```

### 2. Test kiểm tra trạng thái

```bash
curl -X GET http://localhost:8080/api/public/bus-company/registration-status/test@example.com
```

### 3. Test admin APIs (cần token)

```bash
# Đăng nhập admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com", "password": "admin123"}'

# Lấy danh sách đăng ký
curl -X GET http://localhost:8080/api/admin/bus-company/registrations \
  -H "Authorization: Bearer <admin-token>"

# Duyệt đăng ký
curl -X POST http://localhost:8080/api/admin/bus-company/registrations/1/approve \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"admin_notes": "Duyệt thành công"}'
```

## 📊 Database Schema

### BusCompanyRegistration

- id, companyName, email, phoneNumber
- image, descriptions, businessLicense, address
- status (PENDING/APPROVED/REJECTED)
- adminNotes, approvedBy, approvedAt
- createdAt, updatedAt

### BusCompany (tạo sau khi duyệt)

- id, companyName, image, descriptions
- createdAt, updatedAt

### User (tài khoản nhà xe)

- id, email, password, firstName, lastName
- phone, status, roles, busCompany
- createdAt, updatedAt

## 🎯 Kết quả đạt được

✅ **API đăng ký nhà xe hoạt động**: Nhà xe có thể đăng ký thành công
✅ **Email notification**: Gửi email xác nhận và thông báo kết quả
✅ **Admin management**: Admin có thể xem, duyệt, từ chối đăng ký
✅ **Security**: Phân quyền rõ ràng giữa public và admin
✅ **Error handling**: Xử lý lỗi đầy đủ với status code phù hợp
✅ **Documentation**: Tài liệu API chi tiết và hướng dẫn sử dụng

Hệ thống quản lý đăng ký và duyệt nhà xe đã hoàn thiện và sẵn sàng sử dụng!
