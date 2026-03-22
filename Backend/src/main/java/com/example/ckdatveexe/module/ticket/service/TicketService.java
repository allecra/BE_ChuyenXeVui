package com.example.ckdatveexe.module.ticket.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.ticket.dto.*;
import com.example.ckdatveexe.module.discount.service.DiscountService;
import com.example.ckdatveexe.module.discount.dto.ApplyDiscountRequest;
import com.example.ckdatveexe.module.discount.dto.ApplyDiscountResponse;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import com.example.ckdatveexe.shared.util.PaymentProviderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SeatLockRepository seatLockRepository;
    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final TicketEmailService ticketEmailService;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final PaymentRepository paymentRepository;
    private final DiscountService discountService;
    private final PaymentProviderUtil paymentProviderUtil;

    private static final int PAYMENT_TIMEOUT_MINUTES = 5; // 5 phút để thanh toán

    @Transactional
    public BookTicketResponse bookTicket(BookTicketRequest request, Integer userId) {
        log.info("🎫 Booking ticket for user {} (schedule: {}, seat: {})",
                userId, request.getScheduleId(), request.getSeatId());

        // 1. Validate seat availability
        Seat seat = validateSeatAvailability(request.getSeatId(), request.getScheduleId());

        // 2. Validate schedule exists
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại"));

        // 3. Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 4. Create seat lock
        SeatLock lock = createSeatLockForBooking(seat, schedule, userId, request.getSessionId());

        // 5. Create ticket immediately (PENDING status)
        Ticket ticket = new Ticket();
        ticket.setSeat(seat);
        ticket.setSchedule(schedule);
        ticket.setUser(user);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setTicketCode(generateTicketCode());

        // Calculate base price
        Double basePrice = calculatePrice(seat, schedule);
        ticket.setOriginalPrice(basePrice);
        ticket.setPrice(basePrice);
        ticket.setDiscountAmount(0.0);

        // Apply discount if provided
        if (request.getDiscountCode() != null && !request.getDiscountCode().trim().isEmpty()) {
            try {
                ApplyDiscountRequest discountRequest = new ApplyDiscountRequest();
                discountRequest.setDiscountCode(request.getDiscountCode());
                discountRequest.setOrderAmount(BigDecimal.valueOf(basePrice));
                discountRequest.setRouteId(schedule.getRoute().getId());
                discountRequest.setCompanyId(schedule.getRoute().getBusCompany().getId());

                ApplyDiscountResponse discountResponse = discountService.applyDiscount(discountRequest, userId);

                if (discountResponse.isValid()) {
                    ticket.setDiscountAmount(discountResponse.getDiscountAmount().doubleValue());
                    ticket.setPrice(discountResponse.getFinalAmount().doubleValue());
                    // Set discount code reference (will be set after saving discount usage)
                    log.info("💰 Discount applied: {} VND (Code: {})",
                            discountResponse.getDiscountAmount(), request.getDiscountCode());
                } else {
                    log.warn("⚠️ Invalid discount code: {} - {}", request.getDiscountCode(),
                            discountResponse.getMessage());
                    // Continue without discount but log the issue
                }
            } catch (Exception e) {
                log.warn("⚠️ Error applying discount code: {} - {}", request.getDiscountCode(), e.getMessage());
                // Continue without discount
            }
        }

        // Set passenger info (auto-fill from user profile if not provided)
        setPassengerInfo(ticket, user, request.getPassengerInfo());

        // Set payment deadline (5 minutes from now)
        ticket.setPaymentDeadline(LocalDateTime.now().plusMinutes(PAYMENT_TIMEOUT_MINUTES));

        // Set trip times from schedule
        ticket.setDepartureTime(schedule.getDepartureTime());
        ticket.setArrivalTime(schedule.getArrivalTime());
        ticket.setSeatType(seat.getSeatType());

        // 6. Update seat status to LOCKED
        seat.setStatus(SeatStatus.LOCKED);

        // 7. Save all
        seatLockRepository.save(lock);
        ticketRepository.save(ticket);
        seatRepository.save(seat);

        // 8. Schedule cleanup task (5 minutes)
        scheduleTicketCleanup(ticket.getId(), PAYMENT_TIMEOUT_MINUTES);

        // 9. Send booking confirmation email
        ticketEmailService.sendTicketBookingConfirmation(ticket);

        log.info("✅ Ticket booked successfully: {} (expires in {} minutes)",
                ticket.getTicketCode(), PAYMENT_TIMEOUT_MINUTES);

        return BookTicketResponse.fromEntity(ticket);
    }

    private Seat validateSeatAvailability(Integer seatId, Integer scheduleId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new IllegalArgumentException("Ghế không khả dụng - đã có người chọn trước");
        }

        // Check if seat is already locked for this schedule
        seatLockRepository.findActiveLockBySeatAndSchedule(seatId, scheduleId)
                .ifPresent(lock -> {
                    throw new IllegalArgumentException("Ghế đang được giữ bởi người khác");
                });

        return seat;
    }

    private SeatLock createSeatLockForBooking(Seat seat, Schedule schedule, Integer userId, String sessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(PAYMENT_TIMEOUT_MINUTES);

        SeatLock lock = new SeatLock();
        lock.setSeat(seat);
        lock.setSchedule(schedule);
        lock.setUser(user);
        lock.setLockedAt(now);
        lock.setExpiresAt(expiresAt);
        lock.setSessionId(sessionId);
        lock.setStatus(LockStatus.ACTIVE);

        return lock;
    }

    @org.springframework.scheduling.annotation.Async
    public void scheduleTicketCleanup(Integer ticketId, int minutes) {
        java.util.concurrent.CompletableFuture.delayedExecutor(minutes, java.util.concurrent.TimeUnit.MINUTES)
                .execute(() -> autoExpireTicket(ticketId));

        // Schedule payment reminder at 2 minutes remaining (3 minutes after booking)
        if (minutes > 2) {
            java.util.concurrent.CompletableFuture.delayedExecutor(minutes - 2, java.util.concurrent.TimeUnit.MINUTES)
                    .execute(() -> sendPaymentReminder(ticketId, 2));
        }
    }

    @Transactional
    public void sendPaymentReminder(Integer ticketId, int minutesRemaining) {
        log.info("⏰ Sending payment reminder for ticket: {} ({} minutes remaining)", ticketId, minutesRemaining);

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket != null && ticket.getStatus() == TicketStatus.PENDING) {
            ticketEmailService.sendPaymentReminderNotification(ticket, minutesRemaining);
            log.info("✅ Payment reminder sent for ticket: {}", ticket.getTicketCode());
        }
    }

    @Transactional
    public void autoExpireTicket(Integer ticketId) {
        log.info("⏰ Auto-expiring ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket != null && ticket.getStatus() == TicketStatus.PENDING) {
            // Expire ticket
            ticket.setStatus(TicketStatus.EXPIRED);

            // Release seat
            ticket.getSeat().setStatus(SeatStatus.AVAILABLE);

            // Expire related lock
            seatLockRepository.findActiveLockBySeatAndSchedule(
                    ticket.getSeat().getId(), ticket.getSchedule().getId())
                    .ifPresent(lock -> {
                        lock.setStatus(LockStatus.EXPIRED);
                        seatLockRepository.save(lock);
                    });

            ticketRepository.save(ticket);
            seatRepository.save(ticket.getSeat());

            // Send expiration email
            ticketEmailService.sendTicketExpirationNotification(ticket);

            log.info("✅ Ticket expired automatically: {}", ticket.getTicketCode());
        }
    }

    @Transactional
    public TicketResponse createTicketFromLock(CreateTicketRequest request, Integer userId) {
        log.info("🎫 Creating ticket from lock {} for user {}", request.getLockId(), userId);

        // 1. Validate lock còn hiệu lực
        SeatLock lock = validateActiveLock(request.getLockId(), userId);

        // 2. Tạo ticket
        Ticket ticket = new Ticket();
        ticket.setSeat(lock.getSeat());
        ticket.setSchedule(lock.getSchedule());
        ticket.setUser(lock.getUser());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setTicketCode(generateTicketCode());
        ticket.setPrice(calculatePrice(lock.getSeat(), lock.getSchedule()));
        ticket.setOriginalPrice(ticket.getPrice());
        ticket.setDiscountAmount(0.0);

        // Set passenger info
        if (request.getPassengerInfo() != null) {
            setPassengerInfoFromCreateRequest(ticket, lock.getUser(), request.getPassengerInfo());
        } else {
            // Auto-fill from user profile
            setPassengerInfoFromUser(ticket, lock.getUser());
        }

        // Set payment deadline (same as lock expiry)
        ticket.setPaymentDeadline(lock.getExpiresAt());

        // Set trip times from schedule
        ticket.setDepartureTime(lock.getSchedule().getDepartureTime());
        ticket.setArrivalTime(lock.getSchedule().getArrivalTime());
        ticket.setSeatType(lock.getSeat().getSeatType());

        // 3. Cập nhật lock status
        lock.setStatus(LockStatus.CONVERTED);

        // 4. Seat vẫn LOCKED cho đến khi thanh toán
        ticketRepository.save(ticket);
        seatLockRepository.save(lock);

        log.info("✅ Ticket created successfully: {}", ticket.getTicketCode());
        return TicketResponse.fromEntity(ticket);
    }

    private SeatLock validateActiveLock(Integer lockId, Integer userId) {
        return seatLockRepository.findByIdAndUserIdAndStatus(lockId, userId, LockStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Lock không tồn tại hoặc đã hết hạn"));
    }

    private String generateTicketCode() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = ticketRepository.count() + 1;
        return String.format("TK%s%03d", dateStr, count);
    }

    private Double calculatePrice(Seat seat, Schedule schedule) {
        // Base price from seat
        Double basePrice = seat.getPriceForSeatType();

        // TODO: Add route-based pricing logic if needed
        // Could multiply by distance factor, add route surcharge, etc.

        return basePrice;
    }

    @Transactional
    public void confirmTicketPayment(Integer ticketId) {
        log.info("💳 Confirming payment for ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Vé không tồn tại"));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new IllegalArgumentException("Vé không ở trạng thái chờ thanh toán");
        }

        // 1. Cập nhật ticket status
        ticket.setStatus(TicketStatus.CONFIRMED);

        // 2. Cập nhật seat status
        ticket.getSeat().setStatus(SeatStatus.BOOKED);

        // 3. Cập nhật lock status
        seatLockRepository.findActiveLockBySeatAndSchedule(
                ticket.getSeat().getId(), ticket.getSchedule().getId())
                .ifPresent(lock -> {
                    lock.setStatus(LockStatus.CONVERTED);
                    seatLockRepository.save(lock);
                });

        ticketRepository.save(ticket);
        seatRepository.save(ticket.getSeat());

        // 4. Record discount usage if discount was applied
        if (ticket.getDiscountAmount() != null && ticket.getDiscountAmount() > 0) {
            try {
                // Find the discount code that was used (this would need to be stored in ticket)
                // For now, we'll skip this as we need to modify the ticket entity to store
                // discount_code_id
                log.info("💰 Discount was applied: {} VND", ticket.getDiscountAmount());
            } catch (Exception e) {
                log.warn("⚠️ Error recording discount usage: {}", e.getMessage());
            }
        }

        // Send payment success email
        ticketEmailService.sendPaymentSuccessNotification(ticket);

        log.info("✅ Ticket payment confirmed: {}", ticket.getTicketCode());
    }

    @Transactional
    public TicketResponse cancelPendingTicket(Integer ticketId, Integer userId) {
        log.info("❌ Cancelling pending ticket: {} by user: {}", ticketId, userId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Vé không tồn tại"));

        // Verify ticket belongs to user
        if (!ticket.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy vé này");
        }

        // Check if ticket can be cancelled
        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể hủy vé đang chờ thanh toán");
        }

        // Cancel ticket
        ticket.setStatus(TicketStatus.CANCELLED);

        // Release seat
        ticket.getSeat().setStatus(SeatStatus.AVAILABLE);

        // Expire related lock
        seatLockRepository.findActiveLockBySeatAndSchedule(
                ticket.getSeat().getId(), ticket.getSchedule().getId())
                .ifPresent(lock -> {
                    lock.setStatus(LockStatus.EXPIRED);
                    seatLockRepository.save(lock);
                });

        ticketRepository.save(ticket);
        seatRepository.save(ticket.getSeat());

        // Send cancellation email
        ticketEmailService.sendTicketCancellationNotification(ticket);

        log.info("✅ Pending ticket cancelled successfully: {}", ticket.getTicketCode());
        return TicketResponse.fromEntity(ticket);
    }

    public ScheduleSeatsResponse getScheduleSeats(Integer scheduleId) {
        log.info("🎫 Getting seat layout for schedule: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại"));

        // Get all buses for this schedule
        List<ScheduleBus> scheduleBuses = schedule.getScheduleBuses();
        if (scheduleBuses.isEmpty()) {
            throw new IllegalArgumentException("Chuyến xe chưa có xe được gán");
        }

        // For now, take the first bus (could be enhanced to handle multiple buses)
        Bus bus = scheduleBuses.get(0).getBus();

        ScheduleSeatsResponse response = new ScheduleSeatsResponse();
        response.setScheduleId(scheduleId);

        // Set bus info
        ScheduleSeatsResponse.BusInfo busInfo = new ScheduleSeatsResponse.BusInfo();
        busInfo.setBusNumber(bus.getLicensePlate());
        busInfo.setBusType(bus.getBusType().name());
        busInfo.setTotalSeats(bus.getSeats().size());
        response.setBusInfo(busInfo);

        // Get seat information with status
        List<SeatInfoResponse> seats = bus.getSeats().stream()
                .map(seat -> {
                    SeatInfoResponse seatInfo = new SeatInfoResponse();
                    seatInfo.setSeatId(seat.getId());
                    seatInfo.setSeatNumber(seat.getSeatNumber());
                    seatInfo.setSeatType(seat.getSeatType().name());
                    seatInfo.setPrice(seat.getPriceForSeatType());
                    seatInfo.setStatus(seat.getStatus().name());

                    // Set position
                    SeatInfoResponse.SeatPosition position = new SeatInfoResponse.SeatPosition();
                    position.setRow(seat.getRowNumber());
                    position.setColumn(seat.getColumnNumber());
                    seatInfo.setPosition(position);

                    // If locked, get lock expiry time
                    if (seat.getStatus() == SeatStatus.LOCKED) {
                        seatLockRepository.findActiveLockBySeatAndSchedule(seat.getId(), scheduleId)
                                .ifPresent(lock -> seatInfo.setLockedUntil(lock.getExpiresAt()));
                    }

                    return seatInfo;
                })
                .collect(Collectors.toList());

        response.setSeats(seats);
        return response;
    }

    public List<TicketResponse> getUserTickets(Integer userId, String status, Pageable pageable) {
        log.info("📋 Getting tickets for user: {} with status: {}", userId, status);

        List<Ticket> tickets;
        if (status != null && !status.isEmpty()) {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            tickets = ticketRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, ticketStatus);
        } else {
            tickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return tickets.stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public TicketResponse getTicketDetail(Integer ticketId, Integer userId) {
        log.info("🔍 Getting ticket detail: {} for user: {}", ticketId, userId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Vé không tồn tại"));

        // Verify ticket belongs to user
        if (!ticket.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xem vé này");
        }

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    public TicketResponse cancelTicket(Integer ticketId, Integer userId) {
        log.info("❌ Cancelling ticket: {} by user: {}", ticketId, userId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Vé không tồn tại"));

        // Verify ticket belongs to user
        if (!ticket.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy vé này");
        }

        // Check if ticket can be cancelled
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalArgumentException("Vé đã được hủy trước đó");
        }

        if (ticket.getStatus() == TicketStatus.EXPIRED) {
            throw new IllegalArgumentException("Vé đã hết hạn");
        }

        // Check departure time (allow cancellation up to 2 hours before departure)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancellationDeadline = ticket.getDepartureTime().minusHours(2);

        if (now.isAfter(cancellationDeadline)) {
            throw new IllegalArgumentException("Không thể hủy vé trong vòng 2 giờ trước giờ khởi hành");
        }

        // Cancel ticket
        ticket.setStatus(TicketStatus.CANCELLED);

        // Release seat
        ticket.getSeat().setStatus(SeatStatus.AVAILABLE);

        ticketRepository.save(ticket);
        seatRepository.save(ticket.getSeat());

        // Send cancellation email
        ticketEmailService.sendTicketCancellationNotification(ticket);

        log.info("✅ Ticket cancelled successfully: {}", ticket.getTicketCode());
        return TicketResponse.fromEntity(ticket);
    }

    // Company methods
    public List<TicketResponse> getScheduleTickets(Integer scheduleId, Integer companyId, String status,
            Pageable pageable) {
        log.info("📋 Getting tickets for schedule: {} by company: {} with status: {}", scheduleId, companyId, status);

        // TODO: Verify schedule belongs to company

        List<Ticket> tickets;
        if (status != null && !status.isEmpty()) {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            tickets = ticketRepository.findByScheduleIdAndStatus(scheduleId, ticketStatus);
        } else {
            tickets = ticketRepository.findByScheduleIdOrderByCreatedAtDesc(scheduleId);
        }

        return tickets.stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ScheduleSeatsResponse getScheduleSeatsForCompany(Integer scheduleId, Integer companyId) {
        // TODO: Verify schedule belongs to company
        return getScheduleSeats(scheduleId);
    }

    @Transactional
    public TicketResponse bookForCustomer(BookForCustomerRequest request, Integer companyId) {
        log.info("🎫 Booking for customer by company: {} (schedule: {}, seat: {})",
                companyId, request.getScheduleId(), request.getSeatId());

        // 1. Validate seat availability
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new IllegalArgumentException("Ghế không khả dụng");
        }

        // 2. Validate schedule
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại"));

        // TODO: Verify schedule belongs to company

        // 3. Get company user
        User companyUser = userRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Công ty không tồn tại"));

        // 4. Create ticket directly (no lock needed for company booking)
        Ticket ticket = new Ticket();
        ticket.setSeat(seat);
        ticket.setSchedule(schedule);
        ticket.setUser(companyUser); // Company as booker
        ticket.setStatus(TicketStatus.CONFIRMED); // Direct confirmation for company booking
        ticket.setTicketCode(generateTicketCode());
        ticket.setPrice(calculatePrice(seat, schedule));
        ticket.setOriginalPrice(ticket.getPrice());
        ticket.setDiscountAmount(0.0);

        // Set customer info
        if (request.getCustomerInfo() != null) {
            ticket.setPassengerName(request.getCustomerInfo().getFullName());
            ticket.setPassengerPhone(request.getCustomerInfo().getPhoneNumber());
            ticket.setPassengerEmail(request.getCustomerInfo().getEmail());
            ticket.setPassengerIdCard(request.getCustomerInfo().getIdCard());
        }

        // Set trip times
        ticket.setDepartureTime(schedule.getDepartureTime());
        ticket.setArrivalTime(schedule.getArrivalTime());
        ticket.setSeatType(seat.getSeatType());
        ticket.setNotes(request.getNotes());

        // 5. Update seat status
        seat.setStatus(SeatStatus.BOOKED);

        ticketRepository.save(ticket);
        seatRepository.save(seat);

        log.info("✅ Ticket booked for customer: {}", ticket.getTicketCode());
        return TicketResponse.fromEntity(ticket);
    }

    public List<TicketResponse> getCompanyTickets(Integer companyId, String status, Integer scheduleId,
            Pageable pageable) {
        log.info("📋 Getting company tickets: company={}, status={}, schedule={}", companyId, status, scheduleId);

        // TODO: Implement company-specific ticket filtering
        // For now, return empty list as placeholder
        return List.of();
    }

    public TicketResponse getCompanyTicketDetail(Integer ticketId, Integer companyId) {
        log.info("🔍 Getting company ticket detail: {} for company: {}", ticketId, companyId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Vé không tồn tại"));

        // TODO: Verify ticket belongs to company's schedules

        return TicketResponse.fromEntity(ticket);
    }

    public GenerateTicketsResponse generateTicketsForSchedule(Integer scheduleId, Integer companyId) {
        log.info("🎫 Generating tickets for schedule: {} by company: {}", scheduleId, companyId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại"));

        // TODO: Verify schedule belongs to company

        // Get buses for this schedule
        List<ScheduleBus> scheduleBuses = schedule.getScheduleBuses();
        if (scheduleBuses.isEmpty()) {
            throw new IllegalArgumentException("Chuyến xe chưa có xe được gán");
        }

        int totalSeats = 0;
        int ticketsGenerated = 0;

        // Generate tickets for all buses in the schedule
        for (ScheduleBus scheduleBus : scheduleBuses) {
            Bus bus = scheduleBus.getBus();
            totalSeats += bus.getSeats().size();

            // For now, just count seats (actual ticket generation would be implemented
            // here)
            ticketsGenerated += bus.getSeats().size();
        }

        GenerateTicketsResponse response = new GenerateTicketsResponse();
        response.setScheduleId(scheduleId);
        response.setTotalSeats(totalSeats);
        response.setTicketsGenerated(ticketsGenerated);
        response.setAvailableTickets(ticketsGenerated);
        response.setBookedTickets(0);
        response.setMessage("Phát hành vé thành công");

        log.info("✅ Generated {} tickets for schedule: {}", ticketsGenerated, scheduleId);
        return response;
    }

    /**
     * Set passenger information for ticket, auto-filling from user profile if not
     * provided
     */
    private void setPassengerInfo(Ticket ticket, User user, BookTicketRequest.PassengerInfo passengerInfo) {
        if (passengerInfo != null) {
            // Use provided passenger info (allow user to override)
            ticket.setPassengerName(
                    passengerInfo.getFullName() != null ? passengerInfo.getFullName() : getFullNameFromUser(user));
            ticket.setPassengerPhone(
                    passengerInfo.getPhoneNumber() != null ? passengerInfo.getPhoneNumber() : user.getPhone());
            ticket.setPassengerEmail(passengerInfo.getEmail() != null ? passengerInfo.getEmail() : user.getEmail());
            ticket.setPassengerIdCard(passengerInfo.getIdCard() != null ? passengerInfo.getIdCard() : user.getIdCard());
        } else {
            // Auto-fill from user profile
            ticket.setPassengerName(getFullNameFromUser(user));
            ticket.setPassengerPhone(user.getPhone());
            ticket.setPassengerEmail(user.getEmail());
            ticket.setPassengerIdCard(user.getIdCard());
        }

        log.info("🔄 Passenger info set - Name: {}, Phone: {}, Email: {}",
                ticket.getPassengerName(),
                ticket.getPassengerPhone() != null
                        ? "***" + ticket.getPassengerPhone()
                                .substring(Math.max(0, ticket.getPassengerPhone().length() - 3))
                        : "null",
                ticket.getPassengerEmail());
    }

    /**
     * Get full name from user entity
     */
    private String getFullNameFromUser(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        } else if (user.getLastName() != null) {
            return user.getLastName();
        } else {
            return "Khách hàng"; // Default name
        }
    }

    /**
     * Set passenger information from CreateTicketRequest
     */
    private void setPassengerInfoFromCreateRequest(Ticket ticket, User user,
            CreateTicketRequest.PassengerInfo passengerInfo) {
        if (passengerInfo != null) {
            // Use provided passenger info (allow user to override)
            ticket.setPassengerName(
                    passengerInfo.getFullName() != null ? passengerInfo.getFullName() : getFullNameFromUser(user));
            ticket.setPassengerPhone(
                    passengerInfo.getPhoneNumber() != null ? passengerInfo.getPhoneNumber() : user.getPhone());
            ticket.setPassengerEmail(passengerInfo.getEmail() != null ? passengerInfo.getEmail() : user.getEmail());
            ticket.setPassengerIdCard(passengerInfo.getIdCard() != null ? passengerInfo.getIdCard() : user.getIdCard());
        } else {
            // Auto-fill from user profile
            setPassengerInfoFromUser(ticket, user);
        }

        log.info("🔄 Passenger info set from CreateRequest - Name: {}, Phone: {}, Email: {}",
                ticket.getPassengerName(),
                ticket.getPassengerPhone() != null
                        ? "***" + ticket.getPassengerPhone()
                                .substring(Math.max(0, ticket.getPassengerPhone().length() - 3))
                        : "null",
                ticket.getPassengerEmail());
    }

    /**
     * Set passenger information from user profile only
     */
    private void setPassengerInfoFromUser(Ticket ticket, User user) {
        ticket.setPassengerName(getFullNameFromUser(user));
        ticket.setPassengerPhone(user.getPhone());
        ticket.setPassengerEmail(user.getEmail());
        ticket.setPassengerIdCard(user.getIdCard());

        log.info("🔄 Passenger info auto-filled from user profile - Name: {}, Email: {}",
                ticket.getPassengerName(), ticket.getPassengerEmail());
    }

    @Transactional
    public TicketCancellationResponse cancelTicket(TicketCancellationRequest request, Integer userId) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Vé không tồn tại"));

        // Verify ownership
        if (!ticket.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy vé này");
        }

        // Check if ticket can be cancelled
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalArgumentException("Vé đã được hủy trước đó");
        }

        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể hủy vé đã được xác nhận");
        }

        // Check cancellation time limit
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = ticket.getSchedule().getDepartureTime();

        // Get cancellation policy for the route
        CancellationPolicy policy = cancellationPolicyRepository.findByRouteId(ticket.getSchedule().getRoute().getId())
                .orElse(getDefaultCancellationPolicy());

        long hoursUntilDeparture = ChronoUnit.HOURS.between(now, departureTime);

        if (hoursUntilDeparture < policy.getCancellationTimeLimit()) {
            throw new IllegalArgumentException(
                    String.format("Không thể hủy vé. Cần hủy trước %d giờ so với giờ khởi hành",
                            policy.getCancellationTimeLimit()));
        }

        // Calculate refund amount
        BigDecimal originalAmount = BigDecimal.valueOf(ticket.getPrice());
        BigDecimal refundPercentage = BigDecimal.valueOf(policy.getRefundPercentage());
        BigDecimal refundAmount = originalAmount.multiply(refundPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal cancellationFee = originalAmount.subtract(refundAmount);

        // Update ticket status
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setCancellationReason(request.getCancellationReason());
        ticket.setCancellationTime(now);
        ticketRepository.save(ticket);

        // Release seats
        List<Seat> seats = seatRepository.findByTicketId(ticket.getId());
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setTicket(null);
            seatRepository.save(seat);
        }

        // Create refund record (if refund amount > 0)
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            createRefundRecord(ticket, refundAmount, request);
        }

        log.info("Cancelled ticket: {} for user: {}, refund amount: {}",
                ticket.getTicketCode(), userId, refundAmount);

        // Prepare response
        TicketCancellationResponse response = new TicketCancellationResponse();
        response.setTicketId(ticket.getId());
        response.setTicketCode(ticket.getTicketCode());
        response.setOriginalAmount(originalAmount);
        response.setRefundAmount(refundAmount);
        response.setCancellationFee(cancellationFee);
        response.setRefundPercentage(policy.getRefundPercentage());
        response.setCancellationReason(request.getCancellationReason());
        response.setCancellationTime(now);
        response.setEstimatedRefundTime(now.plusDays(7)); // 7 days processing time
        response.setRefundMethod("Chuyển khoản ngân hàng");
        response.setRefundStatus("Đang xử lý");
        response.setMessage("Hủy vé thành công. Tiền hoàn sẽ được chuyển trong vòng 7 ngày làm việc.");

        return response;
    }

    @Transactional
    public TicketModificationResponse modifyTicket(TicketModificationRequest request, Integer userId) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Vé không tồn tại"));

        // Verify ownership
        if (!ticket.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền đổi vé này");
        }

        // Check if ticket can be modified
        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể đổi vé đã được xác nhận");
        }

        // Check modification time limit (24 hours before departure)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = ticket.getSchedule().getDepartureTime();
        long hoursUntilDeparture = ChronoUnit.HOURS.between(now, departureTime);

        if (hoursUntilDeparture < 24) {
            throw new IllegalArgumentException("Không thể đổi vé. Cần đổi trước 24 giờ so với giờ khởi hành");
        }

        Schedule oldSchedule = ticket.getSchedule();
        final Schedule newSchedule;

        if (request.getNewScheduleId() != null) {
            newSchedule = scheduleRepository.findById(request.getNewScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lịch trình mới không tồn tại"));
        } else {
            newSchedule = null;
        }

        // Store old information
        String oldScheduleInfo = String.format("%s - %s (%s)",
                oldSchedule.getRoute().getDepartureLocation(),
                oldSchedule.getRoute().getArrivalLocation(),
                oldSchedule.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        List<Seat> oldSeats = seatRepository.findByTicketId(ticket.getId());
        String oldSeatNumbers = oldSeats.stream()
                .map(Seat::getSeatNumber)
                .collect(Collectors.joining(", "));

        BigDecimal originalAmount = BigDecimal.valueOf(ticket.getPrice());
        BigDecimal newAmount = originalAmount;
        BigDecimal additionalFee = BigDecimal.ZERO;

        // Handle schedule change
        if (newSchedule != null) {
            // Release old seats
            for (Seat seat : oldSeats) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setTicket(null);
                seatRepository.save(seat);
            }

            // Book new seats
            if (request.getNewSeatNumbers() != null && !request.getNewSeatNumbers().isEmpty()) {
                List<Seat> newSeats = new ArrayList<>();
                for (String seatNumber : request.getNewSeatNumbers()) {
                    Seat seat = seatRepository.findByScheduleIdAndSeatNumber(newSchedule.getId(), seatNumber)
                            .orElseThrow(() -> new ResourceNotFoundException("Ghế " + seatNumber + " không tồn tại"));

                    if (seat.getStatus() != SeatStatus.AVAILABLE) {
                        throw new IllegalArgumentException("Ghế " + seatNumber + " không khả dụng");
                    }

                    seat.setStatus(SeatStatus.BOOKED);
                    seat.setTicket(ticket);
                    newSeats.add(seatRepository.save(seat));
                }

                // Calculate new price
                newAmount = BigDecimal.valueOf(newSeats.stream()
                        .mapToDouble(seat -> calculatePrice(seat, newSchedule))
                        .sum());
            }

            ticket.setSchedule(newSchedule);
        }

        // Calculate modification fee (5% of original amount)
        BigDecimal modificationFee = originalAmount.multiply(BigDecimal.valueOf(0.05));
        additionalFee = additionalFee.add(modificationFee);

        // If new amount is higher, add the difference
        if (newAmount.compareTo(originalAmount) > 0) {
            additionalFee = additionalFee.add(newAmount.subtract(originalAmount));
        }

        // Update ticket
        ticket.setPrice(newAmount.doubleValue());
        ticket.setModificationReason(request.getModificationReason());
        ticket.setModificationTime(now);
        ticketRepository.save(ticket);

        // Prepare response
        TicketModificationResponse response = new TicketModificationResponse();
        response.setTicketId(ticket.getId());
        response.setTicketCode(ticket.getTicketCode());
        response.setOriginalAmount(originalAmount);
        response.setNewAmount(newAmount);
        response.setAdditionalFee(additionalFee);
        response.setModificationReason(request.getModificationReason());
        response.setModificationTime(now);
        response.setOldScheduleInfo(oldScheduleInfo);

        if (newSchedule != null) {
            response.setNewScheduleInfo(String.format("%s - %s (%s)",
                    newSchedule.getRoute().getDepartureLocation(),
                    newSchedule.getRoute().getArrivalLocation(),
                    newSchedule.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }

        response.setOldSeatNumbers(oldSeatNumbers);
        if (request.getNewSeatNumbers() != null) {
            response.setNewSeatNumbers(String.join(", ", request.getNewSeatNumbers()));
        }

        response.setMessage("Đổi vé thành công. Phí đổi vé: " + additionalFee + " VND");

        log.info("Modified ticket: {} for user: {}, additional fee: {}",
                ticket.getTicketCode(), userId, additionalFee);

        return response;
    }

    private CancellationPolicy getDefaultCancellationPolicy() {
        CancellationPolicy defaultPolicy = new CancellationPolicy();
        defaultPolicy.setCancellationTimeLimit(24); // 24 hours
        defaultPolicy.setRefundPercentage(80); // 80% refund
        defaultPolicy.setDescriptions("Chính sách hủy vé mặc định: Hủy trước 24h được hoàn 80%");
        return defaultPolicy;
    }

    private void createRefundRecord(Ticket ticket, BigDecimal refundAmount, TicketCancellationRequest request) {
        // Create a payment record for refund tracking
        Payment refundPayment = new Payment();
        refundPayment.setUser(ticket.getUser());
        refundPayment.setTicket(ticket);
        refundPayment.setAmount(refundAmount);
        refundPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        refundPayment.setPaymentProvider(paymentProviderUtil.getSystemProvider());
        refundPayment.setStatus(PaymentStatus.PENDING);
        refundPayment.setTransactionId("REFUND_" + ticket.getTicketCode() + "_" + System.currentTimeMillis());
        refundPayment.setDescription("Hoàn tiền hủy vé: " + ticket.getTicketCode());

        // Store bank account info for refund
        if (request.getBankAccountNumber() != null) {
            refundPayment.setPaymentDetails(String.format(
                    "Bank: %s, Account: %s, Name: %s",
                    request.getBankName(),
                    request.getBankAccountNumber(),
                    request.getBankAccountName()));
        }

        paymentRepository.save(refundPayment);
        log.info("Created refund record: {} for amount: {}", refundPayment.getTransactionId(), refundAmount);
    }
}