# Test Bus Company Registration API

## ✅ Đã sửa các lỗi:

1. **BusCompanyRegistration Entity**:
   - Thêm `@JsonProperty` annotations để đảm bảo JSON serialization
   - Thêm length constraints cho các columns
   - Cải thiện nullable và updatable constraints

2. **BusCompanyRegistrationController**:
   - Xóa duplicate method `checkRegistrationStatus`
   - Giữ lại method ở cuối file (đầy đủ hơn)

## 🧪 Test Cases

### 1. Test API đăng ký nhà xe

```bash
curl -X POST http://localhost:8080/api/public/bus-company/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Nhà xe Test Fix",
    "email": "testfix@example.com",
    "phoneNumber": "0987654321",
    "descriptions": "Test sau khi fix lỗi",
    "businessLicense": "TEST123456",
    "address": "123 Test Street"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Đăng ký nhà xe thành công! Chúng tôi sẽ xem xét và thông báo kết quả qua email trong thời gian sớm nhất.",
  "data": {
    "id": 1,
    "company_name": "Nhà xe Test Fix",
    "email": "testfix@example.com",
    "phone_number": "0987654321",
    "status": "PENDING",
    "created_at": "2026-03-23T21:30:00",
    "message": "Đăng ký thành công! Email xác nhận đã được gửi."
  }
}
```

### 2. Test kiểm tra trạng thái đăng ký

```bash
curl -X GET http://localhost:8080/api/public/bus-company/registration-status/testfix@example.com
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Đơn đăng ký đang được xem xét",
  "data": "PENDING"
}
```

### 3. Test endpoint test

```bash
curl -X GET http://localhost:8080/api/public/bus-company/test
```

**Expected Response:**

```json
{
  "success": true,
  "message": "API is working correctly",
  "data": "Test successful"
}
```

## 📧 Email Test

Sau khi đăng ký thành công, kiểm tra email `thienkt179@gmail.com` sẽ gửi email xác nhận đến địa chỉ đăng ký.

**Email Subject**: "Đăng ký nhà xe thành công - CK DatVeXe"

**Email Content**:

- Xác nhận đã nhận đăng ký
- Thông báo đang xem xét
- Thời gian đăng ký

## 🔧 Troubleshooting

### Nếu API trả về empty response `{}`:

1. Kiểm tra application có start thành công không
2. Kiểm tra logs xem có controller logs không
3. Restart application để load changes

### Nếu 401 Unauthorized:

1. Kiểm tra SecurityConfig đã permitAll cho `/api/public/**`
2. Kiểm tra AuthTokenFilter đã skip public paths
3. Kiểm tra AuthEntryPointJwt không block public paths

### Nếu email không gửi được:

1. Kiểm tra SMTP config trong `.env`
2. Kiểm tra EmailService logs
3. Kiểm tra network connection

## ✅ Kết quả mong đợi

Sau khi fix lỗi:

- ✅ API đăng ký hoạt động bình thường
- ✅ Response trả về đầy đủ thông tin với email
- ✅ Email xác nhận được gửi thành công
- ✅ Không còn lỗi compilation
- ✅ JSON serialization hoạt động đúng

## 🚀 Next Steps

1. Test với real email address
2. Test admin APIs để duyệt đăng ký
3. Test full workflow từ đăng ký → duyệt → tạo tài khoản
4. Deploy và test trên production environment
