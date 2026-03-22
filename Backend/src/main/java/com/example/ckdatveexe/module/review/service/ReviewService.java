package com.example.ckdatveexe.module.review.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.review.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    // ===== USER METHODS =====

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, Integer userId) {
        log.info("⭐ Creating review for ticket {} by user {}", request.getTicketId(), userId);

        // 1. Validate ticket exists and belongs to user
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Vé không tồn tại"));

        if (!ticket.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền đánh giá vé này");
        }

        // 2. Check if ticket is completed (status CONFIRMED and trip has ended)
        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể đánh giá vé đã được xác nhận");
        }

        // 3. Check if review already exists for this ticket
        if (reviewRepository.existsByTicketId(request.getTicketId())) {
            throw new IllegalArgumentException("Bạn đã đánh giá vé này rồi");
        }

        // 4. Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // 5. Create review
        Review review = new Review();
        review.setUser(user);
        review.setTicket(ticket);
        review.setRoute(ticket.getSchedule().getRoute());
        review.setBusCompany(ticket.getSchedule().getRoute().getBusCompany());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setStatus(ReviewStatus.PENDING);

        review = reviewRepository.save(review);

        log.info("✅ Review created successfully: {} stars for ticket {}", request.getRating(), request.getTicketId());
        return ReviewResponse.fromEntity(review);
    }

    public Page<ReviewResponse> getUserReviews(Integer userId, Pageable pageable) {
        log.info("📋 Getting reviews for user: {}", userId);

        Page<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        log.info("✅ Retrieved {} reviews for user", reviews.getTotalElements());
        return reviews.map(ReviewResponse::fromEntity);
    }

    public ReviewResponse getReviewById(Integer id, Integer userId) {
        log.info("🔍 Getting review {} for user {}", id, userId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        // Verify review belongs to user
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xem đánh giá này");
        }

        return ReviewResponse.fromEntity(review);
    }

    // ===== COMPANY METHODS =====

    public Page<ReviewResponse> getCompanyReviews(Integer companyId, ReviewStatus status,
            Integer rating, Pageable pageable) {
        log.info("🏢 Getting reviews for company: {}, status: {}, rating: {}", companyId, status, rating);

        Page<Review> reviews;

        if (rating != null && status != null) {
            reviews = reviewRepository.findByRatingAndStatusOrderByCreatedAtDesc(rating, status, pageable);
        } else if (status != null) {
            reviews = reviewRepository.findByBusCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status, pageable);
        } else if (rating != null) {
            reviews = reviewRepository.findByRatingOrderByCreatedAtDesc(rating, pageable);
        } else {
            reviews = reviewRepository.findByBusCompanyIdOrderByCreatedAtDesc(companyId, pageable);
        }

        log.info("✅ Retrieved {} reviews for company", reviews.getTotalElements());
        return reviews.map(ReviewResponse::fromEntity);
    }

    public ReviewResponse getCompanyReviewById(Integer id, Integer companyId) {
        log.info("🔍 Getting review {} for company {}", id, companyId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        // Verify review belongs to company
        if (!review.getBusCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Đánh giá này không thuộc về công ty của bạn");
        }

        return ReviewResponse.fromEntity(review);
    }

    public Double getCompanyAverageRating(Integer companyId) {
        log.info("📊 Getting average rating for company: {}", companyId);

        Double avgRating = reviewRepository.getAverageRatingByCompanyId(companyId);
        return avgRating != null ? avgRating : 0.0;
    }

    public List<Object[]> getCompanyRatingDistribution(Integer companyId) {
        log.info("📊 Getting rating distribution for company: {}", companyId);

        return reviewRepository.getRatingDistributionByCompanyId(companyId);
    }

    // ===== ADMIN METHODS =====

    public Page<ReviewResponse> getAllReviews(ReviewStatus status, String keyword,
            Integer companyId, Integer rating, Pageable pageable) {
        log.info("👑 Getting all reviews - Status: {}, Keyword: {}, Company: {}, Rating: {}",
                status, keyword, companyId, rating);

        Page<Review> reviews;

        if (keyword != null && !keyword.trim().isEmpty()) {
            if (status != null) {
                reviews = reviewRepository.searchByKeywordAndStatus(keyword.trim(), status, pageable);
            } else {
                reviews = reviewRepository.searchByKeyword(keyword.trim(), pageable);
            }
        } else if (status != null) {
            reviews = reviewRepository.findByStatus(status, pageable);
        } else {
            reviews = reviewRepository.findAll(pageable);
        }

        log.info("✅ Retrieved {} reviews", reviews.getTotalElements());
        return reviews.map(ReviewResponse::fromEntity);
    }

    public Page<ReviewResponse> getPendingReviews(Pageable pageable) {
        log.info("⏳ Getting pending reviews for admin approval");

        Page<Review> reviews = reviewRepository.findPendingReviewsForAdmin(pageable);

        log.info("✅ Retrieved {} pending reviews", reviews.getTotalElements());
        return reviews.map(ReviewResponse::fromEntity);
    }

    @Transactional
    public ReviewResponse approveReview(Integer id, ReviewActionRequest request, Integer adminId) {
        log.info("✅ Approving review {} by admin {}", id, adminId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể duyệt đánh giá đang chờ xử lý");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin không tồn tại"));

        review.approve(admin, request.getAdminNotes());
        review = reviewRepository.save(review);

        log.info("✅ Review approved successfully: {}", id);
        return ReviewResponse.fromEntity(review);
    }

    @Transactional
    public ReviewResponse rejectReview(Integer id, ReviewActionRequest request, Integer adminId) {
        log.info("❌ Rejecting review {} by admin {}", id, adminId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể từ chối đánh giá đang chờ xử lý");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin không tồn tại"));

        review.reject(admin, request.getAdminNotes());
        review = reviewRepository.save(review);

        log.info("✅ Review rejected successfully: {}", id);
        return ReviewResponse.fromEntity(review);
    }

    public ReviewResponse getAdminReviewById(Integer id) {
        log.info("🔍 Getting review {} for admin", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        return ReviewResponse.fromEntity(review);
    }

    // ===== STATISTICS METHODS =====

    public long getReviewCountByStatus(ReviewStatus status) {
        if (status != null) {
            return reviewRepository.countByStatus(status);
        } else {
            return reviewRepository.count();
        }
    }

    public long getCompanyReviewCount(Integer companyId, ReviewStatus status) {
        if (status != null) {
            return reviewRepository.countByBusCompanyIdAndStatus(companyId, status);
        } else {
            return reviewRepository.countByBusCompanyId(companyId);
        }
    }

    public Double getRouteAverageRating(Integer routeId) {
        log.info("📊 Getting average rating for route: {}", routeId);

        Double avgRating = reviewRepository.getAverageRatingByRouteId(routeId);
        return avgRating != null ? avgRating : 0.0;
    }

    public List<Object[]> getRouteRatingDistribution(Integer routeId) {
        log.info("📊 Getting rating distribution for route: {}", routeId);

        return reviewRepository.getRatingDistributionByRouteId(routeId);
    }

    public boolean canUserReviewTicket(Integer userId, Integer ticketId) {
        return reviewRepository.countByUserIdAndTicketId(userId, ticketId) == 0;
    }
}