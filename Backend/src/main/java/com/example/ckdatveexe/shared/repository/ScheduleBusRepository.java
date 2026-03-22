package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.ScheduleBus;
import com.example.ckdatveexe.shared.entity.ScheduleBusStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleBusRepository extends JpaRepository<ScheduleBus, Integer> {

    // Basic queries
    List<ScheduleBus> findByScheduleId(Integer scheduleId);

    List<ScheduleBus> findByScheduleIdAndStatus(Integer scheduleId, ScheduleBusStatus status);

    List<ScheduleBus> findByBusId(Integer busId);

    List<ScheduleBus> findByBusIdAndStatus(Integer busId, ScheduleBusStatus status);

    Optional<ScheduleBus> findByScheduleIdAndBusId(Integer scheduleId, Integer busId);

    // Check if bus is already assigned to schedule
    boolean existsByScheduleIdAndBusId(Integer scheduleId, Integer busId);

    // Time conflict detection for bus scheduling
    @Query("""
            SELECT sb FROM ScheduleBus sb
            JOIN sb.schedule s
            WHERE sb.busId = :busId
            AND sb.status = 'ACTIVE'
            AND s.status IN ('ACTIVE', 'INACTIVE')
            AND s.id != :excludeScheduleId
            AND (
                (s.departureTime < :arrivalTime AND s.arrivalTime > :departureTime)
            )
            """)
    List<ScheduleBus> findConflictingScheduleBuses(@Param("busId") Integer busId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("arrivalTime") LocalDateTime arrivalTime,
            @Param("excludeScheduleId") Integer excludeScheduleId);

    // Get active buses for a schedule
    @Query("""
            SELECT sb FROM ScheduleBus sb
            JOIN FETCH sb.bus b
            WHERE sb.scheduleId = :scheduleId
            AND sb.status = 'ACTIVE'
            AND b.status = 'ACTIVE'
            """)
    List<ScheduleBus> findActiveBusesByScheduleId(@Param("scheduleId") Integer scheduleId);

    // Get all buses for a schedule (for company view)
    @Query("""
            SELECT sb FROM ScheduleBus sb
            JOIN FETCH sb.bus b
            WHERE sb.scheduleId = :scheduleId
            ORDER BY sb.createdAt ASC
            """)
    List<ScheduleBus> findAllBusesByScheduleId(@Param("scheduleId") Integer scheduleId);

    // Check if bus has sold tickets in schedule
    @Query("""
            SELECT COUNT(t) > 0 FROM Ticket t
            JOIN t.schedule s
            JOIN ScheduleBus sb ON sb.scheduleId = s.id
            WHERE sb.busId = :busId
            AND sb.scheduleId = :scheduleId
            AND t.status != 'CANCELLED'
            """)
    boolean hasSoldTicketsForBusInSchedule(@Param("busId") Integer busId,
            @Param("scheduleId") Integer scheduleId);

    // Count active buses in schedule
    long countByScheduleIdAndStatus(Integer scheduleId, ScheduleBusStatus status);

    // Get schedules by bus company
    @Query("""
            SELECT sb FROM ScheduleBus sb
            JOIN sb.bus b
            JOIN sb.schedule s
            WHERE b.company.id = :busCompanyId
            AND sb.status = :status
            """)
    List<ScheduleBus> findByBusCompanyIdAndStatus(@Param("busCompanyId") Integer busCompanyId,
            @Param("status") ScheduleBusStatus status);

    // Delete by schedule and bus
    void deleteByScheduleIdAndBusId(Integer scheduleId, Integer busId);

    // Get bus count by schedule
    @Query("""
            SELECT s.id, COUNT(sb) FROM Schedule s
            LEFT JOIN ScheduleBus sb ON sb.scheduleId = s.id AND sb.status = 'ACTIVE'
            WHERE s.id IN :scheduleIds
            GROUP BY s.id
            """)
    List<Object[]> countActiveBusesByScheduleIds(@Param("scheduleIds") List<Integer> scheduleIds);

    // Get total capacity by schedule
    @Query("""
            SELECT s.id, COALESCE(SUM(b.capacity), 0) FROM Schedule s
            LEFT JOIN ScheduleBus sb ON sb.scheduleId = s.id AND sb.status = 'ACTIVE'
            LEFT JOIN Bus b ON b.id = sb.busId AND b.status = 'ACTIVE'
            WHERE s.id IN :scheduleIds
            GROUP BY s.id
            """)
    List<Object[]> getTotalCapacityByScheduleIds(@Param("scheduleIds") List<Integer> scheduleIds);

    // Check bus availability for schedule time conflict
    @Query("SELECT COUNT(sb) > 0 FROM ScheduleBus sb WHERE sb.bus.id = :busId " +
            "AND sb.status != 'CANCELLED' " +
            "AND ((sb.schedule.departureTime <= :departureTime AND sb.schedule.arrivalTime > :departureTime) " +
            "OR (sb.schedule.departureTime < :arrivalTime AND sb.schedule.arrivalTime >= :arrivalTime) " +
            "OR (sb.schedule.departureTime >= :departureTime AND sb.schedule.arrivalTime <= :arrivalTime))")
    boolean existsByBusIdAndScheduleTimeConflict(@Param("busId") Integer busId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("arrivalTime") LocalDateTime arrivalTime);
}