package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Seat;
import com.example.ckdatveexe.shared.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

    // Tìm ghế theo xe
    List<Seat> findByBusIdOrderByRowNumberAscColumnNumberAsc(Integer busId);

    // Tìm ghế theo xe và status
    List<Seat> findByBusIdAndStatus(Integer busId, SeatStatus status);

    // Tìm ghế theo số ghế và xe
    Optional<Seat> findByBusIdAndSeatNumber(Integer busId, String seatNumber);

    // Đếm số ghế của xe
    long countByBusId(Integer busId);

    // Đếm số ghế theo status
    long countByBusIdAndStatus(Integer busId, SeatStatus status);

    // Xóa tất cả ghế của xe
    void deleteByBusId(Integer busId);

    // Kiểm tra ghế có tồn tại
    boolean existsByBusIdAndSeatNumber(Integer busId, String seatNumber);

    // Tìm ghế theo vị trí
    Optional<Seat> findByBusIdAndRowNumberAndColumnNumber(Integer busId, Integer rowNumber, Integer columnNumber);

    // Lấy số hàng và cột lớn nhất
    @Query("SELECT MAX(s.rowNumber) FROM Seat s WHERE s.bus.id = :busId")
    Integer findMaxRowNumberByBusId(@Param("busId") Integer busId);

    @Query("SELECT MAX(s.columnNumber) FROM Seat s WHERE s.bus.id = :busId")
    Integer findMaxColumnNumberByBusId(@Param("busId") Integer busId);
}