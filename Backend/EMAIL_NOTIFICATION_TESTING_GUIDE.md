# 📧 Hướng dẫn Test Email Notifications cho Đặt vé

## 📋 Tổng quan Email Notifications

Hệ thống gửi email tự động cho các trạng thái đặt vé:

1. **Booking Confirmation** - Xác nhận đặt vé (PENDING)
2. **Payment Success** - Thanh toán thành công (CONFIRMED)
3. **Payment Reminder** - Nhắc nhở thanh toán (2 phút còn lại)
4. **Ticket Cancellation** - Hủy vé (CANCELLED)
5. **Ticket Expiration** - Vé hết hạn (EXPIRED)

## 🔧 Chuẩn bị Test Environment

### 1. Cấu hình Email trong application.properties

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com

# Async Configuration
spring.task.execution.pool.core-size=2
spring.task.execution.pool.max-size=5
spring.task.execution.pool.queue-capacity=100
```

### 2. Tạo App Password cho Gmail

1. Vào Google Account Settings
2. Security → 2-Step Verification
3. App passwords → Generate password
4. Sử dụng password này trong `spring.mail.password`

### 3. Chuẩn bị Test Data

```sql
-- Tạo user test với email thật
INSERT INTO users (email, password, full_name, phone_number, role, status)
VALUES ('your-test-email@gmail.com', '$2a$10$...', 'Test User', '0123456789', 'USER', 'ACTIVE');

-- Đảm bảo có schedule với ghế AVAILABLE
SELECT s.id, s.departure_time, s.arrival_time,
       COUNT(seats.id) as total_seats,
       COUNT(CASE WHEN seats.status = 'AVAILABLE' THEN 1 END) as available_seats
FROM schedules s
JOIN schedule_buses sb ON s.id = sb.schedule_id
JOIN buses b ON sb.bus_id = b.id
JOIN seats ON b.id = seats.bus_id
GROUP BY s.id;
```

## 🎯 Test Cases - Email Notifications

### Test 1: Booking Confirmation Email

**Mục đích:** Test email xác nhận đặt vé

```bash
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 1,
    "passengerInfo": {
      "fullName": "Nguyễn Văn Test",
      "phoneNumber": "0123456789",
      "email": "your-test-email@gmail.com",
      "idCard": "123456789"
    },
    "sessionId": "test_session_001"
  }'
```

**Expected Email Content:**

- ✅ Subject: "Xác nhận đặt vé - TK20260317001 - CK DatVeXe"
- ✅ Thông tin vé: Mã vé, tuyến đường, giờ khởi hành, ghế, giá
- ✅ Trạng thái: "Chờ thanh toán"
- ✅ Hạn thanh toán: 5 phút từ lúc đặt
- ✅ Hướng dẫn thanh toán
- ✅ HTML format với styling đẹp

**Verify:**

```bash
# Check logs
tail -f logs/application.log | grep "📧 Booking confirmation email sent"

# Check database
SELECT * FROM tickets WHERE status = 'PENDING' ORDER BY created_at DESC LIMIT 1;
```

### Test 2: Payment Reminder Email (2 phút còn lại)

**Mục đích:** Test email nhắc nhở thanh toán

**Setup:** Đặt vé và đợi 3 phút (hoặc modify timeout để test nhanh)

```java
// Temporary modify PAYMENT_TIMEOUT_MINUTES = 4 for faster testing
// Payment reminder sẽ được gửi sau 2 phút (4-2=2)
```

**Expected Email Content:**

- ✅ Subject: "Nhắc nhở thanh toán - TK20260317001 - CK DatVeXe"
- ✅ Countdown: "2 phút" còn lại
- ✅ Cảnh báo: "Vé sẽ tự động hủy nếu không thanh toán kịp thời!"
- ✅ Thông tin vé và hướng dẫn thanh toán nhanh
- ✅ Màu đỏ cho phần countdown

**Verify:**

```bash
# Check logs after 2 minutes
tail -f logs/application.log | grep "📧 Payment reminder email sent"
```

### Test 3: Payment Success Email

**Mục đích:** Test email xác nhận thanh toán thành công

```bash
# Simulate payment success (call internal method)
# In real scenario, this would be called by payment gateway callback

# Manual test via service method:
# ticketService.confirmTicketPayment(ticketId);

# Or via payment API when integrated
curl -X POST "http://localhost:8080/api/payments/callback" \
  -H "Content-Type: application/json" \
  -d '{
    "ticketId": 1,
    "status": "SUCCESS",
    "transactionId": "TXN123456"
  }'
```

**Expected Email Content:**

- ✅ Subject: "Thanh toán thành công - TK20260317001 - CK DatVeXe"
- ✅ Celebration emoji: 🎉
- ✅ Thông tin vé đã xác nhận
- ✅ Trạng thái: "Đã xác nhận" (màu xanh)
- ✅ Lưu ý: Có mặt trước 30 phút, mang CMND
- ✅ Màu xanh cho success box

**Verify:**

```bash
# Check logs
tail -f logs/application.log | grep "📧 Payment success email sent"

# Check database
SELECT * FROM tickets WHERE status = 'CONFIRMED' ORDER BY updated_at DESC LIMIT 1;
SELECT * FROM seats WHERE status = 'BOOKED' ORDER BY updated_at DESC LIMIT 1;
```

### Test 4: Ticket Cancellation Email

**Mục đích:** Test email thông báo hủy vé

```bash
# Cancel pending ticket
curl -X POST "http://localhost:8080/api/user/tickets/1/cancel" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Or cancel confirmed ticket (within 2 hours before departure)
curl -X DELETE "http://localhost:8080/api/user/tickets/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Email Content:**

- ✅ Subject: "Hủy vé thành công - TK20260317001 - CK DatVeXe"
- ✅ Thông báo hủy thành công
- ✅ Thông tin vé đã hủy
- ✅ Trạng thái: "Đã hủy" (màu cam)
- ✅ Thông tin hoàn tiền
- ✅ Màu cam cho cancel box

**Verify:**

```bash
# Check logs
tail -f logs/application.log | grep "📧 Cancellation email sent"

# Check database
SELECT * FROM tickets WHERE status = 'CANCELLED' ORDER BY updated_at DESC LIMIT 1;
SELECT * FROM seats WHERE status = 'AVAILABLE' ORDER BY updated_at DESC LIMIT 1;
```

### Test 5: Ticket Expiration Email

**Mục đích:** Test email thông báo vé hết hạn

**Setup:** Đặt vé và đợi 5 phút hoặc chạy scheduled task

```bash
# Method 1: Wait 5 minutes naturally
# Method 2: Manually trigger expiration
# ticketService.autoExpireTicket(ticketId);

# Method 3: Modify payment deadline in database for faster test
UPDATE tickets SET payment_deadline = NOW() - INTERVAL 1 MINUTE WHERE id = 1;
# Then run scheduled cleanup task
```

**Expected Email Content:**

- ✅ Subject: "Vé hết hạn thanh toán - TK20260317001 - CK DatVeXe"
- ✅ Thông báo hết hạn
- ✅ Thông tin vé đã hết hạn
- ✅ Trạng thái: "Hết hạn" (màu đỏ)
- ✅ Gợi ý: Đặt vé mới, chọn chuyến khác
- ✅ Màu đỏ cho expired box

**Verify:**

```bash
# Check logs
tail -f logs/application.log | grep "📧 Expiration email sent"

# Check scheduled task logs
tail -f logs/application.log | grep "⏰ Auto-expiring ticket"

# Check database
SELECT * FROM tickets WHERE status = 'EXPIRED' ORDER BY updated_at DESC LIMIT 1;
SELECT * FROM seat_locks WHERE status = 'EXPIRED' ORDER BY updated_at DESC LIMIT 1;
```

## 🔄 Complete Email Flow Test

### Scenario 1: Đặt vé → Nhắc nhở → Thanh toán thành công

```bash
# 1. Book ticket
curl -X POST "http://localhost:8080/api/user/tickets/book" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "seatId": 2,
    "passengerInfo": {
      "fullName": "Test Complete Flow",
      "email": "your-test-email@gmail.com"
    }
  }'

# 2. Wait 3 minutes → Should receive payment reminder
# 3. Confirm payment before 5 minutes → Should receive success email
```

**Expected Emails:**

1. ✅ Booking confirmation (ngay lập tức)
2. ✅ Payment reminder (sau 3 phút)
3. ✅ Payment success (khi confirm payment)

### Scenario 2: Đặt vé → Hủy vé

```bash
# 1. Book ticket
# 2. Cancel ticket immediately
curl -X POST "http://localhost:8080/api/user/tickets/{ticketId}/cancel"
```

**Expected Emails:**

1. ✅ Booking confirmation
2. ✅ Cancellation notification

### Scenario 3: Đặt vé → Để hết hạn

```bash
# 1. Book ticket
# 2. Wait 5+ minutes without payment
```

**Expected Emails:**

1. ✅ Booking confirmation
2. ✅ Payment reminder (sau 3 phút)
3. ✅ Expiration notification (sau 5 phút)

## 🐛 Troubleshooting Email Issues

### Issue 1: Email không được gửi

**Check:**

```bash
# 1. Verify email configuration
curl -X GET "http://localhost:8080/actuator/configprops" | grep mail

# 2. Check application logs
tail -f logs/application.log | grep -E "(📧|💥|email)"

# 3. Test SMTP connection
telnet smtp.gmail.com 587
```

**Common Solutions:**

- ✅ Kiểm tra App Password Gmail
- ✅ Enable "Less secure app access" (nếu cần)
- ✅ Kiểm tra firewall/network
- ✅ Verify email address format

### Issue 2: Email bị delay hoặc không đến

**Check:**

```bash
# 1. Check async configuration
# 2. Monitor thread pool
# 3. Check email provider limits
```

**Solutions:**

- ✅ Tăng thread pool size
- ✅ Kiểm tra rate limiting
- ✅ Check spam folder

### Issue 3: HTML email không hiển thị đúng

**Check:**

- ✅ MimeMessageHelper với HTML = true
- ✅ UTF-8 encoding
- ✅ CSS inline styles
- ✅ Email client compatibility

## 📊 Email Analytics & Monitoring

### Database Tracking (Optional Enhancement)

```sql
-- Create email_logs table for tracking
CREATE TABLE email_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    ticket_id INT,
    email_type VARCHAR(50),
    recipient_email VARCHAR(255),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SENT',
    error_message TEXT,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id)
);
```

### Monitoring Queries

```sql
-- Email sending statistics
SELECT
    email_type,
    COUNT(*) as total_sent,
    COUNT(CASE WHEN status = 'SENT' THEN 1 END) as successful,
    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed
FROM email_logs
WHERE sent_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY email_type;

-- Recent email activity
SELECT * FROM email_logs
ORDER BY sent_at DESC
LIMIT 20;
```

## 📝 Test Checklist

### Email Content Verification

- [ ] **Subject line** chính xác và có mã vé
- [ ] **Recipient email** đúng với passenger email
- [ ] **HTML formatting** hiển thị đẹp
- [ ] **Vietnamese text** hiển thị đúng (UTF-8)
- [ ] **Ticket information** đầy đủ và chính xác
- [ ] **Route information** lấy từ schedule/route
- [ ] **Timing information** chính xác (departure, deadline)
- [ ] **Status colors** phù hợp với trạng thái
- [ ] **Company branding** consistent (CK DatVeXe)
- [ ] **Contact information** đúng

### Email Delivery Verification

- [ ] **Booking confirmation** gửi ngay sau khi đặt vé
- [ ] **Payment reminder** gửi đúng thời điểm (3 phút)
- [ ] **Payment success** gửi sau khi confirm payment
- [ ] **Cancellation** gửi sau khi hủy vé
- [ ] **Expiration** gửi sau khi vé hết hạn
- [ ] **No duplicate emails** cho cùng 1 event
- [ ] **Async processing** không block main thread
- [ ] **Error handling** graceful khi email fail

### Integration Testing

- [ ] **End-to-end flow** từ đặt vé đến thanh toán
- [ ] **Multiple users** đặt vé cùng lúc
- [ ] **Email với different passenger info**
- [ ] **Error scenarios** (invalid email, SMTP down)
- [ ] **Performance** với volume cao
- [ ] **Scheduled tasks** chạy đúng thời gian

## 🚀 Production Deployment Notes

### Email Configuration

```properties
# Production SMTP (example with SendGrid)
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_SENDGRID_API_KEY

# Or AWS SES
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=YOUR_SES_USERNAME
spring.mail.password=YOUR_SES_PASSWORD
```

### Monitoring & Alerts

- ✅ Setup email delivery monitoring
- ✅ Alert on high failure rates
- ✅ Monitor async thread pool health
- ✅ Track email sending volume
- ✅ Setup bounce/complaint handling

### Security Considerations

- ✅ Use environment variables for credentials
- ✅ Encrypt sensitive email content
- ✅ Implement rate limiting
- ✅ Validate email addresses
- ✅ Handle PII data properly

---

## 📞 Support

Nếu gặp vấn đề trong quá trình test email:

1. **Check logs** đầu tiên: `tail -f logs/application.log | grep email`
2. **Verify configuration** trong application.properties
3. **Test SMTP connection** manually
4. **Check email provider** documentation
5. **Contact team** nếu cần hỗ trợ thêm

**Happy Testing! 📧✨**
