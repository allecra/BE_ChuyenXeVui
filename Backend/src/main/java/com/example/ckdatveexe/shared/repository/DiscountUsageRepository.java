package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.DiscountUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, Integer> {

        // Count usage by user and discount code
        @Query("SELECT COUNT(du) FROM DiscountUsage du WHERE du.user.id = :userId AND du.discountCode.id = :discountCodeId")
        long countByUserIdAndDiscountCodeId(@Param("userId") Integer userId,
                        @Param("discountCodeId") Integer discountCodeId);

        // Find usage by user
        Page<DiscountUsage> findByUserIdOrderByUsedAtDesc(Integer userId, Pageable pageable);

        // Find usage by discount code
        Page<DiscountUsage> findByDiscountCodeIdOrderByUsedAtDesc(Integer discountCodeId, Pageable pageable);

        // Find usage in date range
        @Query("SELECT du FROM DiscountUsage du WHERE du.usedAt BETWEEN :startDate AND :endDate ORDER BY du.usedAt DESC")
        List<DiscountUsage> findByUsedAtBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Get usage statistics
        @Query("SELECT SUM(du.discountAmount) FROM DiscountUsage du WHERE du.discountCode.id = :discountCodeId")
        Double getTotalDiscountAmountByDiscountCodeId(@Param("discountCodeId") Integer discountCodeId);

        @Query("SELECT COUNT(du) FROM DiscountUsage du WHERE du.discountCode.id = :discountCodeId")
        long getTotalUsageCountByDiscountCodeId(@Param("discountCodeId") Integer discountCodeId);
}