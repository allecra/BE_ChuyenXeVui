package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.DiscountCode;
import com.example.ckdatveexe.shared.entity.DiscountScope;
import com.example.ckdatveexe.shared.entity.DiscountStatus;
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
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Integer> {

        // Find by code
        Optional<DiscountCode> findByCode(String code);

        boolean existsByCode(String code);

        boolean existsByCodeAndIdNot(String code, Integer id);

        // Find by status
        Page<DiscountCode> findByStatus(DiscountStatus status, Pageable pageable);

        List<DiscountCode> findByStatus(DiscountStatus status);

        // Find by scope
        Page<DiscountCode> findByScope(DiscountScope scope, Pageable pageable);

        List<DiscountCode> findByScope(DiscountScope scope);

        // Find by company
        @Query("SELECT d FROM DiscountCode d WHERE d.busCompany.id = :companyId")
        Page<DiscountCode> findByBusCompanyId(@Param("companyId") Integer companyId, Pageable pageable);

        @Query("SELECT d FROM DiscountCode d WHERE d.busCompany.id = :companyId AND d.status = :status")
        Page<DiscountCode> findByBusCompanyIdAndStatus(@Param("companyId") Integer companyId,
                        @Param("status") DiscountStatus status,
                        Pageable pageable);

        // Find active discounts
        @Query("SELECT d FROM DiscountCode d WHERE " +
                        "d.status = 'ACTIVE' AND " +
                        "d.startDate <= :now AND " +
                        "d.endDate > :now AND " +
                        "d.usedCount < d.usageLimit")
        List<DiscountCode> findActiveDiscounts(@Param("now") LocalDateTime now);

        // Find active discounts for user (platform + company specific)
        @Query("SELECT d FROM DiscountCode d WHERE " +
                        "d.status = 'ACTIVE' AND " +
                        "d.startDate <= :now AND " +
                        "d.endDate > :now AND " +
                        "d.usedCount < d.usageLimit AND " +
                        "(d.scope = 'PLATFORM' OR " +
                        "(d.scope = 'COMPANY' AND d.busCompany.id = :companyId))")
        List<DiscountCode> findActiveDiscountsForCompany(@Param("now") LocalDateTime now,
                        @Param("companyId") Integer companyId);

        // Search by keyword
        @Query("SELECT d FROM DiscountCode d WHERE " +
                        "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<DiscountCode> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

        // Search by keyword and status
        @Query("SELECT d FROM DiscountCode d WHERE " +
                        "(LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "d.status = :status")
        Page<DiscountCode> searchByKeywordAndStatus(@Param("keyword") String keyword,
                        @Param("status") DiscountStatus status,
                        Pageable pageable);

        // Find expiring discounts (within next 7 days)
        @Query("SELECT d FROM DiscountCode d WHERE " +
                        "d.status = 'ACTIVE' AND " +
                        "d.endDate <= :sevenDaysFromNow AND " +
                        "d.endDate > :now")
        List<DiscountCode> findExpiringDiscounts(@Param("now") LocalDateTime now,
                        @Param("sevenDaysFromNow") LocalDateTime sevenDaysFromNow);

        // Count by status
        long countByStatus(DiscountStatus status);

        // Count by scope
        long countByScope(DiscountScope scope);

        // Find by route
        @Query("SELECT d FROM DiscountCode d WHERE d.route.id = :routeId AND d.status = 'ACTIVE'")
        List<DiscountCode> findActiveDiscountsByRouteId(@Param("routeId") Integer routeId);
}