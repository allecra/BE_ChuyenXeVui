package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Driver;
import com.example.ckdatveexe.shared.entity.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {

        // Find by status
        Page<Driver> findByStatus(DriverStatus status, Pageable pageable);

        List<Driver> findByStatus(DriverStatus status);

        // Find by license number
        Optional<Driver> findByLicenseNumber(String licenseNumber);

        boolean existsByLicenseNumber(String licenseNumber);

        boolean existsByLicenseNumberAndIdNot(String licenseNumber, Integer id);

        // Find by phone
        Optional<Driver> findByPhone(String phone);

        boolean existsByPhone(String phone);

        boolean existsByPhoneAndIdNot(String phone, Integer id);

        // Search by keyword (name, phone, license)
        @Query("SELECT d FROM Driver d WHERE " +
                        "LOWER(d.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "d.phone LIKE CONCAT('%', :keyword, '%') OR " +
                        "d.licenseNumber LIKE CONCAT('%', :keyword, '%')")
        Page<Driver> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

        // Search by keyword and status
        @Query("SELECT d FROM Driver d WHERE " +
                        "(LOWER(d.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "d.phone LIKE CONCAT('%', :keyword, '%') OR " +
                        "d.licenseNumber LIKE CONCAT('%', :keyword, '%')) AND " +
                        "d.status = :status")
        Page<Driver> searchByKeywordAndStatus(@Param("keyword") String keyword,
                        @Param("status") DriverStatus status,
                        Pageable pageable);

        // Find by current bus
        @Query("SELECT d FROM Driver d WHERE d.currentBus.id = :busId")
        Optional<Driver> findByCurrentBusId(@Param("busId") Integer busId);

        // Find available drivers (ACTIVE status and no current bus)
        @Query("SELECT d FROM Driver d WHERE d.status = 'ACTIVE' AND d.currentBus IS NULL")
        List<Driver> findAvailableDrivers();

        // Count by status
        long countByStatus(DriverStatus status);

        // Find drivers with expiring licenses (within next 30 days)
        @Query(value = "SELECT * FROM drivers d WHERE d.license_expiry_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) AND d.status = 'ACTIVE'", nativeQuery = true)
        List<Driver> findDriversWithExpiringLicenses();
}