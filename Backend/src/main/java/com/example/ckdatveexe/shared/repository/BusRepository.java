package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Bus;
import com.example.ckdatveexe.shared.entity.BusStatus;
import com.example.ckdatveexe.shared.entity.BusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Integer> {

        // Tìm xe theo company
        List<Bus> findByCompanyId(Integer companyId);

        Page<Bus> findByCompanyId(Integer companyId, Pageable pageable);

        // Tìm xe theo status
        List<Bus> findByStatus(BusStatus status);

        Page<Bus> findByStatus(BusStatus status, Pageable pageable);

        // Tìm xe theo company và status
        List<Bus> findByCompanyIdAndStatus(Integer companyId, BusStatus status);

        Page<Bus> findByCompanyIdAndStatus(Integer companyId, BusStatus status, Pageable pageable);

        // Tìm xe theo loại xe
        List<Bus> findByBusType(BusType busType);

        Page<Bus> findByBusType(BusType busType, Pageable pageable);

        // Tìm kiếm xe theo tên hoặc biển số
        @Query("SELECT b FROM Bus b WHERE " +
                        "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(b.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "b.status = :status")
        Page<Bus> searchBuses(@Param("keyword") String keyword,
                        @Param("status") BusStatus status,
                        Pageable pageable);

        // Tìm kiếm xe của company
        @Query("SELECT b FROM Bus b WHERE b.company.id = :companyId AND " +
                        "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(b.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "b.status = :status")
        Page<Bus> searchBusesByCompany(@Param("companyId") Integer companyId,
                        @Param("keyword") String keyword,
                        @Param("status") BusStatus status,
                        Pageable pageable);

        // Tìm xe theo biển số
        Optional<Bus> findByLicensePlate(String licensePlate);

        // Đếm số xe của company
        long countByCompanyId(Integer companyId);

        long countByCompanyIdAndStatus(Integer companyId, BusStatus status);

        // Kiểm tra xe có tồn tại với biển số khác không (cho update)
        boolean existsByLicensePlateAndIdNot(String licensePlate, Integer id);

        // Xóa tất cả xe của company (cho hard delete)
        void deleteByCompanyId(Integer companyId);
}