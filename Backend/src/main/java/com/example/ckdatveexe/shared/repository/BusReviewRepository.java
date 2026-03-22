package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.BusReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusReviewRepository extends JpaRepository<BusReview, Integer> {

        Page<BusReview> findByBusIdOrderByCreatedAtDesc(Integer busId, Pageable pageable);

        Page<BusReview> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

        Optional<BusReview> findByBusIdAndUserId(Integer busId, Integer userId);

        boolean existsByBusIdAndUserId(Integer busId, Integer userId);

        @Query("SELECT AVG(br.rating) FROM BusReview br WHERE br.bus.id = :busId")
        BigDecimal getAverageRatingByBusId(@Param("busId") Integer busId);

        @Query("SELECT COUNT(br) FROM BusReview br WHERE br.bus.id = :busId")
        long countReviewsByBusId(@Param("busId") Integer busId);

        @Query("SELECT br.rating, COUNT(br) FROM BusReview br WHERE br.bus.id = :busId GROUP BY br.rating ORDER BY br.rating DESC")
        List<Object[]> getRatingDistributionByBusId(@Param("busId") Integer busId);

        @Query("SELECT COUNT(br) FROM BusReview br WHERE br.bus.id = :busId AND br.rating = :rating")
        long countReviewsByBusIdAndRating(@Param("busId") Integer busId, @Param("rating") Integer rating);

        @Query("SELECT br FROM BusReview br WHERE br.bus.company.id = :companyId ORDER BY br.createdAt DESC")
        Page<BusReview> findByBusCompanyIdOrderByCreatedAtDesc(@Param("companyId") Integer companyId,
                        Pageable pageable);

        @Query("SELECT br FROM BusReview br WHERE br.rating >= :minRating ORDER BY br.createdAt DESC")
        Page<BusReview> findByRatingGreaterThanEqualOrderByCreatedAtDesc(@Param("minRating") Integer minRating,
                        Pageable pageable);

        @Query("SELECT br FROM BusReview br WHERE br.rating <= :maxRating ORDER BY br.createdAt DESC")
        Page<BusReview> findByRatingLessThanEqualOrderByCreatedAtDesc(@Param("maxRating") Integer maxRating,
                        Pageable pageable);
}