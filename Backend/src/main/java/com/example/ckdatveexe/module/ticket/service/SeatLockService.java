package com.example.ckdatveexe.module.ticket.service;

import com.example.ckdatveexe.module.ticket.dto.SeatLockRequest;
import com.example.ckdatveexe.module.ticket.dto.SeatLockResponse;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockService {

    private final SeatLockRepository seatLockRepository;
    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    private static final int LOCK_DURATION_MINUTES = 10;
    private static final int MAX_LOCKS_PER_USER = 3;

    @Transactional
    public SeatLockResponse lockSeat(SeatLockRequest request, Integer userId) {
        log.info("🔒 Attempting to lock seat {} for schedule {} by user {}",
                request.getSeatId(), request.getScheduleId(), userId);

        // 1. Validate seat availability
        Seat seat = validateSeatAvailable(request.getSeatId(), request.getScheduleId());

        // 2. Validate schedule exists and belongs to user's company (if needed)
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại"));

        // 3. Validate user lock limit
        validateUserLockLimit(userId);

        // 4. Create lock record
        SeatLock lock = createSeatLock(seat, schedule, userId, request.getSessionId());

        // 5. Update seat status
        seat.setStatus(SeatStatus.LOCKED);
        seatRepository.save(seat);

        // 6. Schedule cleanup task
        scheduleUnlockTask(lock.getId(), LOCK_DURATION_MINUTES);

        log.info("✅ Seat locked successfully: lockId={}, expires at {}",
                lock.getId(), lock.getExpiresAt());

        return SeatLockResponse.fromEntity(lock);
    }

    private Seat validateSeatAvailable(Integer seatId, Integer scheduleId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new IllegalArgumentException("Ghế không khả dụng");
        }

        // Check if seat is already locked for this schedule
        seatLockRepository.findActiveLockBySeatAndSchedule(seatId, scheduleId)
                .ifPresent(lock -> {
                    throw new IllegalArgumentException("Ghế đang được giữ bởi người khác");
                });

        return seat;
    }

    private void validateUserLockLimit(Integer userId) {
        long activeLocks = seatLockRepository.countActiveLocksByUser(userId);
        if (activeLocks >= MAX_LOCKS_PER_USER) {
            throw new IllegalArgumentException(
                    "Bạn đã giữ quá nhiều ghế. Vui lòng hoàn tất đặt vé hoặc hủy ghế đã giữ");
        }
    }

    private SeatLock createSeatLock(Seat seat, Schedule schedule, Integer userId, String sessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(LOCK_DURATION_MINUTES);

        SeatLock lock = new SeatLock();
        lock.setSeat(seat);
        lock.setSchedule(schedule);
        lock.setUser(user);
        lock.setLockedAt(now);
        lock.setExpiresAt(expiresAt);
        lock.setSessionId(sessionId);
        lock.setStatus(LockStatus.ACTIVE);

        return seatLockRepository.save(lock);
    }

    @Async
    public void scheduleUnlockTask(Integer lockId, int minutes) {
        CompletableFuture.delayedExecutor(minutes, TimeUnit.MINUTES)
                .execute(() -> autoUnlockExpiredSeat(lockId));
    }

    @Transactional
    public void autoUnlockExpiredSeat(Integer lockId) {
        log.info("⏰ Auto-unlocking expired seat lock: {}", lockId);

        SeatLock lock = seatLockRepository.findById(lockId).orElse(null);
        if (lock != null && lock.getStatus() == LockStatus.ACTIVE) {
            // Unlock seat
            lock.setStatus(LockStatus.EXPIRED);
            lock.getSeat().setStatus(SeatStatus.AVAILABLE);

            seatLockRepository.save(lock);
            seatRepository.save(lock.getSeat());

            // Cancel any pending tickets for this lock
            cancelPendingTicketsForLock(lock);

            log.info("✅ Seat unlocked automatically: seatId={}, lockId={}",
                    lock.getSeat().getId(), lockId);
        }
    }

    private void cancelPendingTicketsForLock(SeatLock lock) {
        List<Ticket> pendingTickets = ticketRepository.findBySeatAndScheduleAndStatus(
                lock.getSeat(), lock.getSchedule(), TicketStatus.PENDING);

        for (Ticket ticket : pendingTickets) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            log.info("🎫 Expired pending ticket: {}", ticket.getTicketCode());
        }
    }

    public SeatLockResponse getLockCountdown(Integer lockId, Integer userId) {
        SeatLock lock = seatLockRepository.findByIdAndUserIdAndStatus(lockId, userId, LockStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin giữ ghế"));

        return SeatLockResponse.fromEntity(lock);
    }

    @Transactional
    public void unlockSeat(Integer lockId, Integer userId) {
        log.info("🔓 Manual unlock seat lock: {} by user {}", lockId, userId);

        SeatLock lock = seatLockRepository.findByIdAndUserIdAndStatus(lockId, userId, LockStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin giữ ghế"));

        // Update lock status
        lock.setStatus(LockStatus.EXPIRED);

        // Update seat status
        lock.getSeat().setStatus(SeatStatus.AVAILABLE);

        seatLockRepository.save(lock);
        seatRepository.save(lock.getSeat());

        log.info("✅ Seat unlocked manually: seatId={}, lockId={}",
                lock.getSeat().getId(), lockId);
    }
}