package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.DeletedBus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeletedBusRepository extends JpaRepository<DeletedBus, Integer> {

    // Tìm xe đã xóa theo company
    List<DeletedBus> findByCompanyId(Integer companyId);

    Page<DeletedBus> findByCompanyId(Integer companyId, Pageable pageable);

    // Tìm xe đã xóa theo người xóa
    List<DeletedBus> findByDeletedBy(Integer deletedBy);

    // Tìm xe đã xóa trong khoảng thời gian
    @Query("SELECT db FROM DeletedBus db WHERE db.deletedAt BETWEEN :startDate AND :endDate")
    List<DeletedBus> findByDeletedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Tìm kiếm xe đã xóa
    @Query("SELECT db FROM DeletedBus db WHERE " +
            "LOWER(db.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(db.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<DeletedBus> searchDeletedBuses(@Param("keyword") String keyword, Pageable pageable);

    // Đếm số xe đã xóa của company
    long countByCompanyId(Integer companyId);
}