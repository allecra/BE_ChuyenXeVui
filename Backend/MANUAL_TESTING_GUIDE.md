# Manual Testing Guide - Bus Company Registration API

## Fixed Issues

- ✅ Removed context path `/api` to avoid double path issue
- ✅ Updated all security configurations to handle `/api/public/**` paths
- ✅ Controller now properly mapped to `/api/public/bus-company`

## API Endpoint

**POST** `/api/public/bus-company/register`

**Full URL**: `http://localhost:8080/api/public/bus-company/register`

## Test Request Body

```json
{
  "companyName": "Nhà xe Test",
  "email": "test@example.com",
  "phoneNumber": "0123456789",
  "image": "https://example.com/logo.jpg",
  "descriptions": "Nhà xe test mô tả",
  "businessLicense": "123456789",
  "address": "123 Test Street, Test City"
}
```

## Expected Response

```json
{
  "success": true,
  "message": "Đăng ký nhà xe thành công! Chúng tôi sẽ xem xét và thông báo kết quả qua email trong thời gian sớm nhất.",
  "data": {
    "id": 1,
    "company_name": "Nhà xe Test",
    "email": "test@example.com",
    "phone_number": "0123456789",
    "status": "PENDING",
    "created_at": "2026-03-23T21:23:00",
    "message": "Đăng ký thành công! Email xác nhận đã được gửi."
  }
}
```

## Email Configuration

- SMTP Host: smtp.gmail.com
- From Email: thienkt179@gmail.com
- Email will be sent to the registered email address

## Testing Steps

1. Start the application: `mvn spring-boot:run`
2. Wait for application to fully start (look for "Started CkDatVeXeApplication")
3. Use Postman/curl to send POST request to the endpoint
4. Check application logs for controller and service logs
5. Check email inbox for confirmation email

## Curl Command

```bash
curl -X POST http://localhost:8080/api/public/bus-company/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Nhà xe Test",
    "email": "test@example.com",
    "phoneNumber": "0123456789"
  }'
```
