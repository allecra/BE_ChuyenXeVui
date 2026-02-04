package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Schedule;
import com.example.ckdatveexe.shared.entity.ScheduleStatus;
import com.example.ckdatveexe.shared.entity.BusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    // Basic queries
    Optional<Schedule> findByIdAndStatus(Integer id, ScheduleStatus status);

    List<Schedule> findByStatus(ScheduleStatus status);

    Page<Schedule> findByStatus(ScheduleStatus status, Pageable pageable);

    // Company-specific queries
    @Query("SELECT s FROM Schedule s WHERE s.bus.company.id = :busCompanyId")
    Page<Schedule> findByBusCompanyId(@Param("busCompanyId") Integer busCompanyId, Pageable pageable);

    @Query("SELECT s FROM Schedule s WHERE s.bus.company.id = :busCompanyId AND s.status = :status")
    Page<Schedule> findByBusCompanyIdAndStatus(@Param("busCompanyId") Integer busCompanyId,
            @Param("status") ScheduleStatus status,
            Pageable pageable);

    // Route-specific queries
    List<Schedule> findByRouteIdAndStatus(Integer routeId, ScheduleStatus status);

    Page<Schedule> findByRouteIdAndStatus(Integer routeId, ScheduleStatus status, Pageable pageable);

    // Bus-specific queries
    List<Schedule> findByBusIdAndStatus(Integer busId, ScheduleStatus status);

    Page<Schedule> findByBusIdAndStatus(Integer busId, ScheduleStatus status, Pageable pageable);

    // Time conflict detection for bus scheduling
    @Query("""
            SELECT s FROM Schedule s
            WHERE s.bus.id = :busId
            AND s.status IN ('ACTIVE', 'INACTIVE')
            AND s.id != :excludeScheduleId
            AND (
                (s.departureTime < :arrivalTime AND s.arrivalTime > :departureTime)
            )
            """)
    List<Schedule> findConflictingSchedules(@Param("busId") Integer busId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("arrivalTime") LocalDateTime arrivalTime,
            @Param("excludeScheduleId") Integer excludeScheduleId);

    // User search query with complex filters - updated for multi-bus support
    @Query("""
            SELECT DISTINCT s FROM Schedule s
            JOIN s.route r
            LEFT JOIN ScheduleBus sb ON sb.scheduleId = s.id AND sb.status = 'ACTIVE'
            LEFT JOIN Bus b ON b.id = sb.busId AND b.status = 'ACTIVE'
            WHERE s.status = 'ACTIVE'
            AND s.departureTime > :currentTime
            AND r.status = 'ACTIVE'
            AND (sb.id IS NOT NULL OR s.bus IS NOT NULL)
            AND (:startStationId IS NULL OR s.startStationId = :startStationId)
            AND (:endStationId IS NULL OR s.endStationId = :endStationId)
            AND (:departureDate IS NULL OR DATE(s.departureTime) = DATE(:departureDate))
            AND (:timeFrom IS NULL OR TIME(s.departureTime) >= TIME(:timeFrom))
            AND (:timeTo IS NULL OR TIME(s.departureTime) <= TIME(:timeTo))
            AND (:minPrice IS NULL OR s.price >= :minPrice)
            AND (:maxPrice IS NULL OR s.price <= :maxPrice)
            AND (:busCompanyId IS NULL OR r.busCompany.id = :busCompanyId)
            AND (:busType IS NULL OR (b.busType = :busType OR (s.bus IS NOT NULL AND s.bus.busType = :busType)))
            AND (:minSeats IS NULL OR s.totalSeats >= :minSeats)
            """)
    Page<Schedule> searchSchedulesForUser(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("startStationId") Integer startStationId,
            @Param("endStationId") Integer endStationId,
            @Param("departureDate") LocalDateTime departureDate,
            @Param("timeFrom") LocalDateTime timeFrom,
            @Param("timeTo") LocalDateTime timeTo,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("busCompanyId") Integer busCompanyId,
            @Param("busType") BusType busType,
            @Param("minSeats") Integer minSeats,
            Pageable pageable);

    // Company search query
    @Query("""
            SELECT s FROM Schedule s
            JOIN s.route r
            JOIN s.bus b
            WHERE b.company.id = :busCompanyId
            AND (:routeId IS NULL OR r.id = :routeId)
            AND (:busId IS NULL OR b.id = :busId)
            AND (:startStationId IS NULL OR s.startStationId = :startStationId)
            AND (:endStationId IS NULL OR s.endStationId = :endStationId)
            AND (:departureTimeFrom IS NULL OR s.departureTime >= :departureTimeFrom)
            AND (:departureTimeTo IS NULL OR s.departureTime <= :departureTimeTo)
            AND (:arrivalTimeFrom IS NULL OR s.arrivalTime >= :arrivalTimeFrom)
            AND (:arrivalTimeTo IS NULL OR s.arrivalTime <= :arrivalTimeTo)
            AND (:status IS NULL OR s.status = :status)
            AND (:minPrice IS NULL OR s.price >= :minPrice)
            AND (:maxPrice IS NULL OR s.price <= :maxPrice)
            """)
    Page<Schedule> searchSchedulesForCompany(
            @Param("busCompanyId") Integer busCompanyId,
            @Param("routeId") Integer routeId,
            @Param("busId") Integer busId,
            @Param("startStationId") Integer startStationId,
            @Param("endStationId") Integer endStationId,
            @Param("departureTimeFrom") LocalDateTime departureTimeFrom,
            @Param("departureTimeTo") LocalDateTime departureTimeTo,
            @Param("arrivalTimeFrom") LocalDateTime arrivalTimeFrom,
            @Param("arrivalTimeTo") LocalDateTime arrivalTimeTo,
            @Param("status") ScheduleStatus status,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    // Check if schedule has sold tickets
    @Query("""
            SELECT COUNT(t) > 0 FROM Ticket t
            WHERE t.schedule.id = :scheduleId
            AND t.status != 'CANCELLED'
            """)
    boolean hasSoldTickets(@Param("scheduleId") Integer scheduleId);

    // Count schedules by various criteria
    long countByBusIdAndStatus(Integer busId, ScheduleStatus status);

    long countByRouteIdAndStatus(Integer routeId, ScheduleStatus status);

    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.bus.company.id = :busCompanyId AND s.status = :status")
    long countByBusCompanyIdAndStatus(@Param("busCompanyId") Integer busCompanyId,
            @Param("status") ScheduleStatus status);

    // Find schedules by station pair
    @Query("""
            SELECT s FROM Schedule s
            WHERE s.startStationId = :startStationId
            AND s.endStationId = :endStationId
            AND s.status = 'ACTIVE'
            AND s.departureTime > :currentTime
            ORDER BY s.departureTime
            """)
    List<Schedule> findByStationPair(@Param("startStationId") Integer startStationId,
            @Param("endStationId") Integer endStationId,
            @Param("currentTime") LocalDateTime currentTime);

    // Find upcoming schedules for a bus
    @Query("""
            SELECT s FROM Schedule s
            WHERE s.bus.id = :busId
            AND s.status = 'ACTIVE'
            AND s.departureTime > :currentTime
            ORDER BY s.departureTime
            """)
    List<Schedule> findUpcomingSchedulesByBus(@Param("busId") Integer busId,
            @Param("currentTime") LocalDateTime currentTime);

    // Find schedules by time range
    @Query("""
            SELECT s FROM Schedule s
            WHERE s.departureTime >= :startTime
            AND s.departureTime <= :endTime
            AND s.status = :status
            ORDER BY s.departureTime
            """)
    List<Schedule> findByTimeRangeAndStatus(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") ScheduleStatus status);

    // Statistics queries
    @Query("""
            SELECT COUNT(s) FROM Schedule s
            WHERE s.bus.company.id = :busCompanyId
            AND s.departureTime >= :startDate
            AND s.departureTime <= :endDate
            AND s.status = :status
            """)
    long countSchedulesByCompanyAndDateRange(@Param("busCompanyId") Integer busCompanyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") ScheduleStatus status);
}