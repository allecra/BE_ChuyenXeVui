package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.LockStatus;
import com.example.ckdatveexe.shared.entity.Schedule;
import com.example.ckdatveexe.shared.entity.Seat;
import com.example.ckdatveexe.shared.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatLockRepository extends JpaRepository<SeatLock, Integer> {

    // Find active lock for specific seat and schedule
    @Query("SELECT sl FROM SeatLock sl WHERE sl.seat.id = :seatId AND sl.schedule.id = :scheduleId AND sl.status = 'ACTIVE'")
    Optional<SeatLock> findActiveLockBySeatAndSchedule(@Param("seatId") Integer seatId,
            @Param("scheduleId") Integer scheduleId);

    // Find all active locks for a user
    @Query("SELECT sl FROM SeatLock sl WHERE sl.user.id = :userId AND sl.status = 'ACTIVE'")
    List<SeatLock> findActiveLocksByUser(@Param("userId") Integer userId);

    // Find expired locks
    List<SeatLock> findByStatusAndExpiresAtBefore(LockStatus status, LocalDateTime dateTime);

    // Find locks by seat and schedule
    List<SeatLock> findBySeatAndScheduleAndStatus(Seat seat, Schedule schedule, LockStatus status);

    // Find lock by user and status
    Optional<SeatLock> findByIdAndUserIdAndStatus(Integer lockId, Integer userId, LockStatus status);

    // Count active locks by user
    @Query("SELECT COUNT(sl) FROM SeatLock sl WHERE sl.user.id = :userId AND sl.status = 'ACTIVE'")
    long countActiveLocksByUser(@Param("userId") Integer userId);

    // Find locks expiring soon (for cleanup)
    @Query("SELECT sl FROM SeatLock sl WHERE sl.status = 'ACTIVE' AND sl.expiresAt <= :dateTime")
    List<SeatLock> findLocksExpiringBefore(@Param("dateTime") LocalDateTime dateTime);
}