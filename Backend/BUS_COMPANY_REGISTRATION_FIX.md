# 🔧 Bus Company Registration API - Null Response Fix

## 🚨 Vấn đề

- Frontend gửi POST request đến `/api/public/bus-company/register`
- Backend trả về status 200 OK nhưng body = null
- Controller method không được gọi

## 🔍 Nguyên nhân đã tìm thấy

**ROOT CAUSE**: Method `registerBusCompany` thiếu annotation `@PostMapping("/register")`

## ✅ Giải pháp đã áp dụng

### 1. Thêm @PostMapping annotation

```java
@PostMapping("/register")
public ResponseEntity<ApiResponse<BusCompanyRegistrationResponse>> registerBusCompany(
    @RequestBody BusCompanyRegistrationRequest request) {
    // method implementation
}
```

### 2. Sửa Jackson configuration

```yaml
jackson:
  serialization:
    write-dates-as-timestamps: false
  default-property-inclusion: always # Changed from non_null
```

### 3. Loại bỏ @JsonFormat có thể gây conflict

Removed from `BusCompanyRegistrationResponse.createdAt`

## 🧪 Test Cases

1. **POST /api/public/bus-company/register** - Main registration endpoint
2. **GET /api/public/bus-company/test-string** - Simple string response test
3. **GET /api/public/bus-company/test-simple-map** - Map response test

## 📋 Checklist

- [x] Thêm @PostMapping("/register") annotation
- [x] Sửa Jackson configuration (non_null → always)
- [x] Loại bỏ @JsonFormat annotation
- [x] Restart application
- [ ] Test với frontend
- [ ] Verify controller logs xuất hiện
- [ ] Verify response body không null

## 🔄 Next Steps

1. Đợi application restart hoàn tất
2. Test API với frontend
3. Kiểm tra logs: `🚀 [CONTROLLER] === BUS COMPANY REGISTRATION START ===`
4. Verify response body có data

## 📝 Expected Logs

Khi API hoạt động đúng, sẽ thấy logs:

```
🚀 [CONTROLLER] === BUS COMPANY REGISTRATION START ===
🚀 [CONTROLLER] Received bus company registration request: Test Company - test@example.com
📝 [SERVICE] Starting bus company registration for: Test Company - test@example.com
✅ [CONTROLLER] Service call completed successfully
```

## 🎯 Expected Response

```json
{
  "success": true,
  "message": "Đăng ký nhà xe thành công! Chúng tôi sẽ xem xét và thông báo kết quả qua email trong thời gian sớm nhất.",
  "data": {
    "id": 1,
    "company_name": "Test Company",
    "email": "test@example.com",
    "phone_number": "0123456789",
    "status": "PENDING",
    "created_at": "2026-03-23T22:48:08",
    "message": "Đăng ký thành công! Email xác nhận đã được gửi."
  }
}
```
