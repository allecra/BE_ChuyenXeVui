# 🎫 Thiết kế Hệ thống API Đặt vé Xe khách

## 📋 Tổng quan Hệ thống

### Workflow Đặt vé:

```
User chọn chuyến → Xem ghế → Chọn ghế → Lock ghế (10 phút) → Thanh toán → Tạo ticket
```

### Roles:

- **USER**: Khách hàng đặt vé online
- **COMPANY**: Nhà xe quản lý vé và đặt vé hộ khách

---

## 🏗️ 1. Entity Design & Relationships

### 1.1 Cập nhật SeatStatus Enum

```java
public enum SeatStatus {
    AVAILABLE,    // Ghế trống, có thể đặt
    LOCKED,       // Ghế đang được giữ (10 phút)
    BOOKED,       // Ghế đã được đặt và thanh toán
    MAINTENANCE,  // Ghế đang bảo trì
    DELETED       // Ghế đã bị xóa mềm
}
```

### 1.2 Cập nhật TicketStatus Enum

```java
public enum TicketStatus {
    PENDING,      // Vé đang chờ thanh toán (ghế đã lock)
    CONFIRMED,    // Vé đã thanh toán thành công
    CANCELLED,    // Vé đã bị hủy
    EXPIRED       // Vé hết hạn (không thanh toán trong 10 phút)
}
```

### 1.3 Entity mới: SeatLock

```java
@Entity
@Table(name = "seat_locks")
public class SeatLock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "session_id")
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LockStatus status = LockStatus.ACTIVE;
}

public enum LockStatus {
    ACTIVE,    // Lock đang hoạt động
    EXPIRED,   // Lock đã hết hạn
    CONVERTED  // Lock đã chuyển thành ticket
}
```

### 1.4 Entity Relationships

```
User (1) ←→ (N) SeatLock
User (1) ←→ (N) Ticket
Schedule (1) ←→ (N) SeatLock
Schedule (1) ←→ (N) Ticket
Seat (1) ←→ (N) SeatLock
Seat (1) ←→ (N) Ticket
Ticket (1) ←→ (N) Payment
```

---

## 🌐 2. API Endpoints Design

### 2.1 USER APIs - Quy trình đặt vé

#### **Bước 1: Xem ghế của chuyến**

```
GET /api/user/schedules/{scheduleId}/seats
```

**Response:**

```json
{
  "success": true,
  "data": {
    "scheduleId": 1,
    "busInfo": {
      "busNumber": "29B-12345",
      "busType": "LIMOUSINE",
      "totalSeats": 40
    },
    "seats": [
      {
        "seatId": 1,
        "seatNumber": "A1",
        "seatType": "VIP",
        "price": 350000,
        "status": "AVAILABLE",
        "position": { "row": 1, "column": 1 }
      },
      {
        "seatId": 2,
        "seatNumber": "A2",
        "seatType": "VIP",
        "price": 350000,
        "status": "LOCKED",
        "lockedUntil": "2026-03-11T08:15:00"
      },
      {
        "seatId": 3,
        "seatNumber": "A3",
        "seatType": "VIP",
        "price": 350000,
        "status": "BOOKED"
      }
    ]
  }
}
```

#### **Bước 2: Lock ghế (Đặt ghế tạm thời)**

```
POST /api/user/seats/lock
```

**Request:**

```json
{
  "scheduleId": 1,
  "seatId": 1,
  "sessionId": "user_session_123"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "lockId": 1,
    "seatId": 1,
    "seatNumber": "A1",
    "scheduleId": 1,
    "lockedAt": "2026-03-11T08:05:00",
    "expiresAt": "2026-03-11T08:15:00",
    "remainingSeconds": 600,
    "price": 350000
  }
}
```

#### **Bước 3: Kiểm tra thời gian còn lại**

```
GET /api/user/seats/lock/{lockId}/countdown
```

**Response:**

```json
{
  "success": true,
  "data": {
    "lockId": 1,
    "remainingSeconds": 450,
    "expiresAt": "2026-03-11T08:15:00",
    "status": "ACTIVE"
  }
}
```

#### **Bước 4: Tạo ticket từ lock**

```
POST /api/user/tickets/create-from-lock
```

**Request:**

```json
{
  "lockId": 1,
  "passengerInfo": {
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0123456789",
    "email": "nguyenvana@example.com"
  }
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "ticketId": 1,
    "ticketCode": "TK20260311001",
    "scheduleId": 1,
    "seatNumber": "A1",
    "price": 350000,
    "status": "PENDING",
    "passengerInfo": {
      "fullName": "Nguyễn Văn A",
      "phoneNumber": "0123456789"
    },
    "paymentRequired": true,
    "paymentDeadline": "2026-03-11T08:15:00"
  }
}
```

#### **Bước 5: Thanh toán vé**

```
POST /api/user/tickets/{ticketId}/payment
```

**Request:**

```json
{
  "provider": "MOMO",
  "returnUrl": "http://localhost:3000/payment/success"
}
```

#### **Bước 6: Hủy vé**

```
POST /api/user/tickets/{ticketId}/cancel
```

**Response:**

```json
{
  "success": true,
  "data": {
    "ticketId": 1,
    "status": "CANCELLED",
    "refundAmount": 350000,
    "seatReleased": true
  }
}
```

#### **Quản lý vé cá nhân**

```
GET /api/user/tickets                    # Danh sách vé của user
GET /api/user/tickets/{ticketId}         # Chi tiết vé
POST /api/user/seats/unlock/{lockId}     # Hủy lock ghế thủ công
```

### 2.2 COMPANY APIs - Quản lý vé và đặt vé hộ

#### **Quản lý vé theo chuyến**

```
GET /api/company/schedules/{scheduleId}/tickets        # Tất cả vé của chuyến
GET /api/company/schedules/{scheduleId}/seats          # Sơ đồ ghế của chuyến
GET /api/company/tickets?status=CONFIRMED              # Lọc vé theo trạng thái
GET /api/company/tickets/{ticketId}                    # Chi tiết vé
```

#### **Đặt vé hộ khách**

```
POST /api/company/tickets/book-for-customer
```

**Request:**

```json
{
  "scheduleId": 1,
  "seatId": 1,
  "customerInfo": {
    "fullName": "Trần Thị B",
    "phoneNumber": "0987654321",
    "email": "tranthib@example.com",
    "idCard": "123456789"
  },
  "paymentMethod": "CASH",
  "notes": "Khách đặt tại quầy"
}
```

#### **Phát hành vé cho chuyến mới**

```
POST /api/company/schedules/{scheduleId}/generate-tickets
```

**Response:**

```json
{
  "success": true,
  "data": {
    "scheduleId": 1,
    "totalSeats": 40,
    "ticketsGenerated": 40,
    "availableTickets": 40
  }
}
```

---

## ⚙️ 3. Business Logic Implementation

### 3.1 Seat Lock Mechanism

#### **Lock Process:**

```java
@Service
public class SeatLockService {

    @Transactional
    public SeatLockResponse lockSeat(SeatLockRequest request) {
        // 1. Kiểm tra ghế có available không
        Seat seat = validateSeatAvailable(request.getSeatId(), request.getScheduleId());

        // 2. Kiểm tra user có lock ghế khác chưa hết hạn không
        validateUserLockLimit(request.getUserId());

        // 3. Tạo lock record
        SeatLock lock = createSeatLock(seat, request);

        // 4. Cập nhật seat status = LOCKED
        seat.setStatus(SeatStatus.LOCKED);
        seatRepository.save(seat);

        // 5. Schedule cleanup task
        scheduleUnlockTask(lock.getId(), 10); // 10 minutes

        return SeatLockResponse.fromEntity(lock);
    }

    @Async
    public void scheduleUnlockTask(Integer lockId, int minutes) {
        CompletableFuture.delayedExecutor(minutes, TimeUnit.MINUTES)
            .execute(() -> autoUnlockExpiredSeat(lockId));
    }

    public void autoUnlockExpiredSeat(Integer lockId) {
        SeatLock lock = seatLockRepository.findById(lockId).orElse(null);
        if (lock != null && lock.getStatus() == LockStatus.ACTIVE) {
            // Unlock seat
            lock.setStatus(LockStatus.EXPIRED);
            lock.getSeat().setStatus(SeatStatus.AVAILABLE);

            seatLockRepository.save(lock);
            seatRepository.save(lock.getSeat());
        }
    }
}
```

### 3.2 Ticket Creation Workflow

#### **From Lock to Ticket:**

```java
@Service
public class TicketService {

    @Transactional
    public TicketResponse createTicketFromLock(CreateTicketRequest request) {
        // 1. Validate lock còn hiệu lực
        SeatLock lock = validateActiveLock(request.getLockId());

        // 2. Tạo ticket
        Ticket ticket = new Ticket();
        ticket.setSeat(lock.getSeat());
        ticket.setSchedule(lock.getSchedule());
        ticket.setUser(lock.getUser());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setTicketCode(generateTicketCode());
        ticket.setPrice(calculatePrice(lock.getSeat(), lock.getSchedule()));

        // 3. Cập nhật lock status
        lock.setStatus(LockStatus.CONVERTED);

        // 4. Seat vẫn LOCKED cho đến khi thanh toán

        ticketRepository.save(ticket);
        seatLockRepository.save(lock);

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    public void confirmTicketPayment(Integer ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();

        // 1. Cập nhật ticket status
        ticket.setStatus(TicketStatus.CONFIRMED);

        // 2. Cập nhật seat status
        ticket.getSeat().setStatus(SeatStatus.BOOKED);

        ticketRepository.save(ticket);
        seatRepository.save(ticket.getSeat());
    }
}
```

### 3.3 Scheduled Cleanup Tasks

```java
@Component
public class SeatLockCleanupTask {

    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void cleanupExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();

        List<SeatLock> expiredLocks = seatLockRepository
            .findByStatusAndExpiresAtBefore(LockStatus.ACTIVE, now);

        for (SeatLock lock : expiredLocks) {
            // Unlock seat
            lock.setStatus(LockStatus.EXPIRED);
            lock.getSeat().setStatus(SeatStatus.AVAILABLE);

            // Hủy ticket pending nếu có
            List<Ticket> pendingTickets = ticketRepository
                .findBySeatAndScheduleAndStatus(
                    lock.getSeat(),
                    lock.getSchedule(),
                    TicketStatus.PENDING
                );

            for (Ticket ticket : pendingTickets) {
                ticket.setStatus(TicketStatus.EXPIRED);
                ticketRepository.save(ticket);
            }
        }

        seatLockRepository.saveAll(expiredLocks);
    }
}
```

---

## 🔄 4. Complete Workflow Examples

### 4.1 User Booking Flow

```bash
# Bước 1: Xem ghế
curl GET /api/user/schedules/1/seats

# Bước 2: Lock ghế
curl -X POST /api/user/seats/lock \
  -H "Authorization: Bearer JWT_TOKEN" \
  -d '{"scheduleId": 1, "seatId": 5}'

# Bước 3: Tạo ticket
curl -X POST /api/user/tickets/create-from-lock \
  -H "Authorization: Bearer JWT_TOKEN" \
  -d '{
    "lockId": 1,
    "passengerInfo": {
      "fullName": "Nguyễn Văn A",
      "phoneNumber": "0123456789"
    }
  }'

# Bước 4: Thanh toán
curl -X POST /api/user/tickets/1/payment \
  -H "Authorization: Bearer JWT_TOKEN" \
  -d '{"provider": "MOMO"}'
```

### 4.2 Company Booking for Customer

```bash
# Đặt vé hộ khách (bỏ qua lock, tạo ticket trực tiếp)
curl -X POST /api/company/tickets/book-for-customer \
  -H "Authorization: Bearer COMPANY_JWT_TOKEN" \
  -d '{
    "scheduleId": 1,
    "seatId": 10,
    "customerInfo": {
      "fullName": "Trần Thị B",
      "phoneNumber": "0987654321"
    },
    "paymentMethod": "CASH"
  }'
```

---

## 📊 5. Database Schema Updates

### 5.1 Migration: Create seat_locks table

```sql
-- V16__Create_seat_locks_table.sql
CREATE TABLE seat_locks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seat_id INT NOT NULL,
    schedule_id INT NOT NULL,
    user_id INT NOT NULL,
    locked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    session_id VARCHAR(255),
    status ENUM('ACTIVE', 'EXPIRED', 'CONVERTED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_seat_schedule (seat_id, schedule_id),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_status (status),

    FOREIGN KEY (seat_id) REFERENCES seats(id),
    FOREIGN KEY (schedule_id) REFERENCES schedules(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 5.2 Migration: Update enums

```sql
-- V17__Update_seat_and_ticket_status.sql
ALTER TABLE seats MODIFY COLUMN status
    ENUM('AVAILABLE', 'LOCKED', 'BOOKED', 'MAINTENANCE', 'DELETED')
    NOT NULL DEFAULT 'AVAILABLE';

ALTER TABLE tickets MODIFY COLUMN status
    ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED')
    NOT NULL DEFAULT 'PENDING';
```

---

## 🎯 6. Key Features Summary

### ✅ User Features:

- Xem sơ đồ ghế real-time
- Lock ghế 10 phút
- Countdown timer
- Tạo vé và thanh toán
- Hủy vé trước giờ khởi hành
- Lịch sử đặt vé

### ✅ Company Features:

- Quản lý tất cả vé theo chuyến
- Đặt vé hộ khách hàng
- Phát hành vé cho chuyến mới
- Thống kê doanh thu
- Quản lý ghế bảo trì

### ✅ System Features:

- Auto-unlock ghế sau 10 phút
- Cleanup expired locks
- Concurrent booking protection
- Real-time seat status
- Payment integration
- Audit trail

---

## 🚀 Implementation Priority

### Phase 1: Core Booking

1. SeatLock entity & repository
2. Seat locking APIs
3. Basic ticket creation
4. Cleanup scheduled tasks

### Phase 2: User Experience

1. Real-time seat status
2. Countdown timer
3. Payment integration
4. Ticket management

### Phase 3: Company Features

1. Company booking APIs
2. Ticket management dashboard
3. Revenue reporting
4. Advanced seat management

Hệ thống này đảm bảo tính nhất quán dữ liệu, tránh double booking, và cung cấp trải nghiệm người dùng mượt mà với cơ chế lock ghế 10 phút!
