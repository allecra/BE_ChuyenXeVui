package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Seat;
import com.example.ckdatveexe.shared.entity.SeatStatus;
import com.example.ckdatveexe.shared.entity.SeatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

        // Basic queries
        List<Seat> findByBusIdOrderByRowNumberAscColumnNumberAsc(Integer busId);

        Page<Seat> findByBusIdAndStatusNotOrderByRowNumberAscColumnNumberAsc(
                        Integer busId, SeatStatus excludeStatus, Pageable pageable);

        Page<Seat> findByBusIdOrderByRowNumberAscColumnNumberAsc(Integer busId, Pageable pageable);

        Optional<Seat> findByIdAndBusCompanyId(Integer seatId, Integer companyId);

        // Check if seat exists with same number in same bus
        boolean existsByBusIdAndSeatNumber(Integer busId, String seatNumber);

        boolean existsByBusIdAndSeatNumberAndIdNot(Integer busId, String seatNumber, Integer seatId);

        // Count seats by status
        long countByBusIdAndStatus(Integer busId, SeatStatus status);

        // Advanced search for BUS_COMPANY
        @Query("SELECT s FROM Seat s JOIN s.bus b WHERE " +
                        "(:busId IS NULL OR s.bus.id = :busId) AND " +
                        "(:companyId IS NULL OR b.company.id = :companyId) AND " +
                        "(:status IS NULL OR s.status = :status) AND " +
                        "(:seatType IS NULL OR s.seatType = :seatType) AND " +
                        "(:minPrice IS NULL OR s.priceForSeatType >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR s.priceForSeatType <= :maxPrice) AND " +
                        "(:seatNumber IS NULL OR LOWER(s.seatNumber) LIKE LOWER(CONCAT('%', :seatNumber, '%')))")
        Page<Seat> searchSeatsForCompany(@Param("busId") Integer busId,
                        @Param("companyId") Integer companyId,
                        @Param("status") SeatStatus status,
                        @Param("seatType") SeatType seatType,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        @Param("seatNumber") String seatNumber,
                        Pageable pageable);

        // Search for USER (exclude DELETED seats)
        @Query("SELECT s FROM Seat s WHERE " +
                        "s.bus.id = :busId AND " +
                        "s.status != 'DELETED' AND " +
                        "(:status IS NULL OR s.status = :status) AND " +
                        "(:seatType IS NULL OR s.seatType = :seatType) AND " +
                        "(:minPrice IS NULL OR s.priceForSeatType >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR s.priceForSeatType <= :maxPrice) AND " +
                        "(:seatNumber IS NULL OR LOWER(s.seatNumber) LIKE LOWER(CONCAT('%', :seatNumber, '%')))")
        Page<Seat> searchSeatsForUser(@Param("busId") Integer busId,
                        @Param("status") SeatStatus status,
                        @Param("seatType") SeatType seatType,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        @Param("seatNumber") String seatNumber,
                        Pageable pageable);

        // Get seat with bus company validation
        @Query("SELECT s FROM Seat s JOIN s.bus b WHERE s.id = :seatId AND b.company.id = :companyId")
        Optional<Seat> findByIdAndCompanyId(@Param("seatId") Integer seatId, @Param("companyId") Integer companyId);

        // Get seat for user (exclude DELETED)
        @Query("SELECT s FROM Seat s WHERE s.id = :seatId AND s.status != 'DELETED'")
        Optional<Seat> findByIdForUser(@Param("seatId") Integer seatId);

        // Delete by bus ID (for bus deletion)
        void deleteByBusId(Integer busId);

        // Statistics queries
        @Query("SELECT COUNT(s) FROM Seat s JOIN s.bus b WHERE b.company.id = :companyId AND s.status = :status")
        long countByCompanyIdAndStatus(@Param("companyId") Integer companyId, @Param("status") SeatStatus status);

        @Query("SELECT s.seatType, COUNT(s) FROM Seat s JOIN s.bus b WHERE b.company.id = :companyId GROUP BY s.seatType")
        List<Object[]> countSeatTypesByCompanyId(@Param("companyId") Integer companyId);

        // Find seat by bus and seat number
        Optional<Seat> findByBusIdAndSeatNumber(Integer busId, String seatNumber);

        // Find seats by ticket ID
        @Query("SELECT s FROM Seat s WHERE s.ticket.id = :ticketId")
        List<Seat> findByTicketId(@Param("ticketId") Integer ticketId);

        // Find seat by schedule and seat number
        @Query("SELECT s FROM Seat s WHERE s.bus.id IN (SELECT sb.bus.id FROM ScheduleBus sb WHERE sb.schedule.id = :scheduleId) AND s.seatNumber = :seatNumber")
        Optional<Seat> findByScheduleIdAndSeatNumber(@Param("scheduleId") Integer scheduleId,
                        @Param("seatNumber") String seatNumber);
}