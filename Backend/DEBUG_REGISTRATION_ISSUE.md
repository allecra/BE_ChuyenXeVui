# Debug Registration Response Issue

## 🔍 Vấn đề hiện tại

- API đăng ký nhà xe hoạt động (status 200/201)
- Frontend nhận được response nhưng body data = null
- Backend logs cho thấy controller được gọi thành công

## 🧪 Test Cases để Debug

### 1. Test endpoint đơn giản

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

### 2. Test registration response format

```bash
curl -X GET http://localhost:8080/api/public/bus-company/test-registration
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Test registration response successful",
  "data": {
    "id": 999,
    "company_name": "Test Company",
    "email": "test@example.com",
    "phone_number": "0123456789",
    "status": "PENDING",
    "created_at": "2026-03-23T21:45:00",
    "message": "Test registration response"
  }
}
```

### 3. Test actual registration

```bash
curl -X POST http://localhost:8080/api/public/bus-company/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Debug Test Company",
    "email": "debug@test.com",
    "phoneNumber": "0987654321"
  }'
```

## 🔧 Possible Issues & Solutions

### Issue 1: Jackson LocalDateTime Serialization

**Problem**: LocalDateTime không serialize đúng
**Solution**: Thêm Jackson configuration

### Issue 2: ApiResponse Generic Type Issue

**Problem**: Generic type bị mất trong serialization
**Solution**: Kiểm tra ApiResponse class

### Issue 3: Lombok Builder Issue

**Problem**: @Builder có thể gây vấn đề với Jackson
**Solution**: Thêm explicit constructors

### Issue 4: JSON Property Mapping

**Problem**: @JsonProperty không hoạt động đúng
**Solution**: Kiểm tra Jackson configuration

## 🚀 Debug Steps

1. **Check simple test endpoint** - Xem API cơ bản có hoạt động không
2. **Check registration test endpoint** - Xem response format có đúng không
3. **Check actual registration** - So sánh với test endpoint
4. **Check backend logs** - Xem controller logs chi tiết
5. **Check network tab** - Xem raw HTTP response

## 🔍 Expected Logs

Khi gọi registration API, backend logs sẽ hiển thị:

```
🚀 [CONTROLLER] === BUS COMPANY REGISTRATION START ===
🚀 [CONTROLLER] Received bus company registration request: Debug Test Company - debug@test.com
✅ [VALIDATION] All validations passed, calling service...
📝 [SERVICE] Starting bus company registration for: Debug Test Company - debug@test.com
📧 [EMAIL] Attempting to send registration confirmation email to: debug@test.com
✅ [CONTROLLER] Service call completed successfully
✅ [CONTROLLER] Response data: BusCompanyRegistrationResponse(id=1, companyName=Debug Test Company, ...)
✅ [CONTROLLER] Final API response: ApiResponse(success=true, message=..., data=...)
🚀 [CONTROLLER] === BUS COMPANY REGISTRATION END ===
```

## 🎯 Quick Fix Options

### Option 1: Simplify Response (Temporary)

Tạm thời loại bỏ LocalDateTime để test:

```java
@JsonProperty("created_at")
private String createdAt; // Use String instead of LocalDateTime
```

### Option 2: Add Jackson Config

Thêm configuration cho LocalDateTime:

```java
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
private LocalDateTime createdAt;
```

### Option 3: Manual JSON Response

Tạm thời trả về Map thay vì object:

```java
Map<String, Object> responseData = new HashMap<>();
responseData.put("id", savedRegistration.getId());
responseData.put("email", savedRegistration.getEmail());
// ...
```

## 🔄 Testing Workflow

1. Start application
2. Test `/test` endpoint → Should work
3. Test `/test-registration` endpoint → Check if LocalDateTime serializes
4. Test actual registration → Compare with test
5. Check browser network tab for raw response
6. Check backend logs for detailed info

Nếu test endpoints hoạt động nhưng registration không, vấn đề nằm ở service layer hoặc database interaction.
