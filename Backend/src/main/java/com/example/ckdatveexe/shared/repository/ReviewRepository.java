package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Review;
import com.example.ckdatveexe.shared.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

        // Find by user
        Page<Review> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

        List<Review> findByUserIdOrderByCreatedAtDesc(Integer userId);

        // Find by ticket
        Optional<Review> findByTicketId(Integer ticketId);

        boolean existsByTicketId(Integer ticketId);

        // Find by status
        Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

        List<Review> findByStatus(ReviewStatus status);

        // Find by company
        Page<Review> findByBusCompanyIdOrderByCreatedAtDesc(Integer companyId, Pageable pageable);

        Page<Review> findByBusCompanyIdAndStatusOrderByCreatedAtDesc(Integer companyId, ReviewStatus status,
                        Pageable pageable);

        // Find by route
        Page<Review> findByRouteIdOrderByCreatedAtDesc(Integer routeId, Pageable pageable);

        Page<Review> findByRouteIdAndStatusOrderByCreatedAtDesc(Integer routeId, ReviewStatus status,
                        Pageable pageable);

        // Find by rating
        Page<Review> findByRatingOrderByCreatedAtDesc(Integer rating, Pageable pageable);

        Page<Review> findByRatingAndStatusOrderByCreatedAtDesc(Integer rating, ReviewStatus status, Pageable pageable);

        // Search by keyword (comment content)
        @Query("SELECT r FROM Review r WHERE " +
                        "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<Review> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

        // Search by keyword and status
        @Query("SELECT r FROM Review r WHERE " +
                        "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
                        "r.status = :status")
        Page<Review> searchByKeywordAndStatus(@Param("keyword") String keyword,
                        @Param("status") ReviewStatus status,
                        Pageable pageable);

        // Get average rating by company
        @Query("SELECT AVG(r.rating) FROM Review r WHERE r.busCompany.id = :companyId AND r.status = 'APPROVED'")
        Double getAverageRatingByCompanyId(@Param("companyId") Integer companyId);

        // Get average rating by route
        @Query("SELECT AVG(r.rating) FROM Review r WHERE r.route.id = :routeId AND r.status = 'APPROVED'")
        Double getAverageRatingByRouteId(@Param("routeId") Integer routeId);

        // Count by status
        long countByStatus(ReviewStatus status);

        // Count by company and status
        long countByBusCompanyIdAndStatus(Integer companyId, ReviewStatus status);

        // Count by company (all statuses)
        long countByBusCompanyId(Integer companyId);

        // Count by route and status
        long countByRouteIdAndStatus(Integer routeId, ReviewStatus status);

        // Get rating distribution by company
        @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.busCompany.id = :companyId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
        List<Object[]> getRatingDistributionByCompanyId(@Param("companyId") Integer companyId);

        // Get rating distribution by route
        @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.route.id = :routeId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
        List<Object[]> getRatingDistributionByRouteId(@Param("routeId") Integer routeId);

        // Find pending reviews for admin
        @Query("SELECT r FROM Review r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
        Page<Review> findPendingReviewsForAdmin(Pageable pageable);

        // Find reviews by user and company
        @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.busCompany.id = :companyId ORDER BY r.createdAt DESC")
        List<Review> findByUserIdAndCompanyId(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

        // Check if user can review ticket
        @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId AND r.ticket.id = :ticketId")
        long countByUserIdAndTicketId(@Param("userId") Integer userId, @Param("ticketId") Integer ticketId);
}