package com.example.ckdatveexe.module.ticket.service;

import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockCleanupService {

    private final SeatLockRepository seatLockRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public void cleanupExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();

        // Find all expired locks
        List<SeatLock> expiredLocks = seatLockRepository.findByStatusAndExpiresAtBefore(LockStatus.ACTIVE, now);

        if (expiredLocks.isEmpty()) {
            log.debug("🔒 No expired seat locks found");
            return;
        }

        log.info("🔒 Found {} expired seat locks to cleanup", expiredLocks.size());

        for (SeatLock lock : expiredLocks) {
            try {
                cleanupExpiredLock(lock);
            } catch (Exception e) {
                log.error("💥 Failed to cleanup expired lock: {}", lock.getId(), e);
            }
        }

        log.info("✅ Completed cleanup of {} expired seat locks", expiredLocks.size());
    }

    private void cleanupExpiredLock(SeatLock lock) {
        log.debug("🔒 Cleaning up expired lock: {} for seat: {}", lock.getId(), lock.getSeat().getSeatNumber());

        // 1. Update lock status
        lock.setStatus(LockStatus.EXPIRED);

        // 2. Release seat
        lock.getSeat().setStatus(SeatStatus.AVAILABLE);

        // 3. Cancel any pending tickets for this lock
        List<Ticket> pendingTickets = ticketRepository.findBySeatAndScheduleAndStatus(
                lock.getSeat(), lock.getSchedule(), TicketStatus.PENDING);

        for (Ticket ticket : pendingTickets) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            log.debug("🎫 Expired pending ticket: {}", ticket.getTicketCode());
        }

        // 4. Save changes
        seatLockRepository.save(lock);
        seatRepository.save(lock.getSeat());

        log.debug("✅ Cleaned up expired lock: {} for seat: {}", lock.getId(), lock.getSeat().getSeatNumber());
    }

    @Transactional
    public void cleanupExpiredLocksForSchedule(Integer scheduleId) {
        log.info("🔒 Cleaning up expired locks for schedule: {}", scheduleId);

        LocalDateTime now = LocalDateTime.now();
        List<SeatLock> expiredLocks = seatLockRepository.findLocksExpiringBefore(now)
                .stream()
                .filter(lock -> lock.getSchedule().getId().equals(scheduleId))
                .toList();

        for (SeatLock lock : expiredLocks) {
            cleanupExpiredLock(lock);
        }

        log.info("✅ Cleaned up {} expired locks for schedule: {}", expiredLocks.size(), scheduleId);
    }
}