# Hướng dẫn API Duyệt Nhà Xe - Bus Company Approval Guide

## Tổng quan

Hệ thống quản lý duyệt đăng ký nhà xe bao gồm các bước:

1. **Đăng ký**: Nhà xe đăng ký qua API public
2. **Xem xét**: Admin xem danh sách và chi tiết đăng ký
3. **Duyệt/Từ chối**: Admin duyệt hoặc từ chối đăng ký
4. **Thông báo**: Hệ thống gửi email thông báo kết quả

## API Endpoints

### 1. Lấy danh sách đăng ký nhà xe

**GET** `/api/admin/bus-company/registrations`

**Headers:**

```
Authorization: Bearer <admin-jwt-token>
```

**Query Parameters:**

- `status` (optional): PENDING, APPROVED, REJECTED
- `companyName` (optional): Tên công ty để lọc
- `page` (default: 0): Trang hiện tại
- `size` (default: 10): Số lượng mỗi trang
- `sortBy` (default: createdAt): Trường sắp xếp
- `sortDirection` (default: desc): Hướng sắp xếp

**Example:**

```bash
GET /api/admin/bus-company/registrations?status=PENDING&page=0&size=10
```

**Response:**

```json
{
  "success": true,
  "message": "Lấy danh sách đăng ký nhà xe thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "companyName": "Nhà xe ABC",
        "email": "contact@abc.com",
        "phoneNumber": "0123456789",
        "status": "PENDING",
        "createdAt": "2026-03-23T10:00:00",
        "descriptions": "Mô tả nhà xe...",
        "businessLicense": "123456789",
        "address": "123 Main St"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

### 2. Tìm kiếm đăng ký nhà xe

**GET** `/api/admin/bus-company/registrations/search`

**Query Parameters:**

- `searchTerm` (required): Từ khóa tìm kiếm (tên công ty hoặc ID)
- `status` (optional): Lọc theo trạng thái
- `page`, `size`, `sortBy`, `sortDirection`: Tương tự API trên

**Example:**

```bash
GET /api/admin/bus-company/registrations/search?searchTerm=ABC&status=PENDING
```

### 3. Xem chi tiết đăng ký

**GET** `/api/admin/bus-company/registrations/{registrationId}`

**Example:**

```bash
GET /api/admin/bus-company/registrations/1
```

**Response:**

```json
{
  "success": true,
  "message": "Lấy chi tiết đăng ký thành công",
  "data": {
    "id": 1,
    "companyName": "Nhà xe ABC",
    "email": "contact@abc.com",
    "phoneNumber": "0123456789",
    "image": "https://example.com/logo.jpg",
    "descriptions": "Nhà xe chuyên tuyến liên tỉnh...",
    "businessLicense": "123456789",
    "address": "123 Main Street, City",
    "status": "PENDING",
    "createdAt": "2026-03-23T10:00:00",
    "updatedAt": "2026-03-23T10:00:00"
  }
}
```

### 4. Duyệt đăng ký nhà xe

**POST** `/api/admin/bus-company/registrations/{registrationId}/approve`

**Request Body (optional):**

```json
{
  "admin_notes": "Đăng ký đầy đủ thông tin, duyệt thành công"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Duyệt đăng ký nhà xe thành công! Tài khoản đã được tạo và email thông báo đã được gửi."
}
```

**Kết quả khi duyệt:**

- Tạo BusCompany entity mới
- Tạo User account với role ROLE_BUS_COMPANY
- Tạo mật khẩu tạm thời
- Gửi email thông báo kèm thông tin đăng nhập
- Cập nhật trạng thái đăng ký thành APPROVED

### 5. Từ chối đăng ký nhà xe

**POST** `/api/admin/bus-company/registrations/{registrationId}/reject`

**Request Body (required):**

```json
{
  "reason": "Thông tin giấy phép kinh doanh không hợp lệ. Vui lòng cung cấp giấy phép đúng quy định."
}
```

**Response:**

```json
{
  "success": true,
  "message": "Từ chối đăng ký nhà xe thành công! Email thông báo đã được gửi."
}
```

**Kết quả khi từ chối:**

- Cập nhật trạng thái đăng ký thành REJECTED
- Gửi email thông báo lý do từ chối
- Lưu lý do từ chối vào adminNotes

## Workflow Duyệt Nhà Xe

### Bước 1: Nhà xe đăng ký

```bash
POST /api/public/bus-company/register
{
    "companyName": "Nhà xe ABC",
    "email": "contact@abc.com",
    "phoneNumber": "0123456789",
    "descriptions": "Mô tả nhà xe",
    "businessLicense": "123456789",
    "address": "Địa chỉ nhà xe"
}
```

### Bước 2: Admin xem danh sách đăng ký chờ duyệt

```bash
GET /api/admin/bus-company/registrations?status=PENDING
```

### Bước 3: Admin xem chi tiết đăng ký

```bash
GET /api/admin/bus-company/registrations/1
```

### Bước 4a: Duyệt đăng ký (nếu hợp lệ)

```bash
POST /api/admin/bus-company/registrations/1/approve
{
    "admin_notes": "Thông tin đầy đủ, duyệt thành công"
}
```

### Bước 4b: Từ chối đăng ký (nếu không hợp lệ)

```bash
POST /api/admin/bus-company/registrations/1/reject
{
    "reason": "Giấy phép kinh doanh không hợp lệ"
}
```

## Email Notifications

### Email xác nhận đăng ký (gửi ngay khi đăng ký)

- **Subject**: "Đăng ký nhà xe thành công - CK DatVeXe"
- **Content**: Xác nhận đã nhận đăng ký, đang xem xét

### Email thông báo duyệt (khi admin approve)

- **Subject**: "Đăng ký nhà xe được duyệt - Thông tin tài khoản - CK DatVeXe"
- **Content**: Thông tin đăng nhập (email + mật khẩu tạm thời)

### Email thông báo từ chối (khi admin reject)

- **Subject**: "Đăng ký nhà xe bị từ chối - CK DatVeXe"
- **Content**: Lý do từ chối, hướng dẫn đăng ký lại

## Quyền truy cập

- **Public**: Chỉ có thể đăng ký (`/api/public/bus-company/register`)
- **Admin**: Có thể xem, duyệt, từ chối tất cả đăng ký
- **Bus Company**: Không có quyền truy cập vào API duyệt

## Status Flow

```
PENDING (Chờ duyệt)
    ↓
APPROVED (Đã duyệt) → Tạo tài khoản + Email thông báo
    ↓
REJECTED (Bị từ chối) → Email thông báo lý do
```

## Error Handling

- **400 Bad Request**: Dữ liệu không hợp lệ
- **401 Unauthorized**: Chưa đăng nhập hoặc token hết hạn
- **403 Forbidden**: Không có quyền admin
- **404 Not Found**: Đăng ký không tồn tại
- **500 Internal Server Error**: Lỗi hệ thống

## Testing với Postman/curl

### 1. Đăng nhập admin để lấy token

```bash
POST /api/auth/login
{
    "email": "admin@example.com",
    "password": "admin123"
}
```

### 2. Sử dụng token để truy cập API admin

```bash
curl -X GET "http://localhost:8080/api/admin/bus-company/registrations" \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json"
```

### 3. Duyệt đăng ký

```bash
curl -X POST "http://localhost:8080/api/admin/bus-company/registrations/1/approve" \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"admin_notes": "Duyệt thành công"}'
```
