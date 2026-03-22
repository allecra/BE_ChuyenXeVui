package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Ticket;
import com.example.ckdatveexe.shared.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

        // Find by ticket code
        Optional<Ticket> findByTicketCode(String ticketCode);

        // Find by user
        List<Ticket> findByUserIdOrderByCreatedAtDesc(Integer userId);

        // Find by schedule
        List<Ticket> findByScheduleIdOrderByCreatedAtDesc(Integer scheduleId);

        // Find by status
        List<Ticket> findByStatus(TicketStatus status);

        // Find by user and status
        List<Ticket> findByUserIdAndStatusOrderByCreatedAtDesc(Integer userId, TicketStatus status);

        // Find by schedule and status
        List<Ticket> findByScheduleIdAndStatus(Integer scheduleId, TicketStatus status);

        // Find by date range
        @Query("SELECT t FROM Ticket t WHERE t.departureTime >= :startDate AND t.departureTime <= :endDate ORDER BY t.departureTime ASC")
        List<Ticket> findByDepartureDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Count tickets by status
        long countByStatus(TicketStatus status);

        // Count tickets by schedule
        long countByScheduleId(Integer scheduleId);

        // Count tickets by user
        long countByUserId(Integer userId);

        // Check if seat is booked for schedule
        @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.schedule.id = :scheduleId AND t.seat.id = :seatId AND t.status IN ('BOOKED', 'CONFIRMED')")
        boolean isSeatBookedForSchedule(@Param("scheduleId") Integer scheduleId, @Param("seatId") Integer seatId);

        // Find available seats for schedule
        @Query("SELECT s.id FROM Seat s WHERE s.bus.id = :busId AND s.id NOT IN (SELECT t.seat.id FROM Ticket t WHERE t.schedule.id = :scheduleId AND t.status IN ('BOOKED', 'CONFIRMED'))")
        List<Integer> findAvailableSeatIdsForSchedule(@Param("scheduleId") Integer scheduleId,
                        @Param("busId") Integer busId);

        // Sum revenue by status
        @Query("SELECT COALESCE(SUM(t.price), 0) FROM Ticket t WHERE t.status = :status")
        Double sumRevenueByStatus(@Param("status") TicketStatus status);

        // Sum revenue by schedule
        @Query("SELECT COALESCE(SUM(t.price), 0) FROM Ticket t WHERE t.schedule.id = :scheduleId AND t.status = 'CONFIRMED'")
        Double sumRevenueBySchedule(@Param("scheduleId") Integer scheduleId);

        // Find tickets by seat, schedule and status
        List<Ticket> findBySeatAndScheduleAndStatus(
                        com.example.ckdatveexe.shared.entity.Seat seat,
                        com.example.ckdatveexe.shared.entity.Schedule schedule,
                        TicketStatus status);
}