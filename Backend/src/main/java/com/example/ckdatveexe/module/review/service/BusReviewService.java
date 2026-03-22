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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusReviewService {

    private final BusReviewRepository busReviewRepository;
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public BusReviewResponse createReview(BusReviewCreateRequest request, Integer userId) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if bus exists
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Xe buýt không tồn tại"));

        // Check if user has already reviewed this bus
        if (busReviewRepository.existsByBusIdAndUserId(request.getBusId(), userId)) {
            throw new IllegalArgumentException("Bạn đã đánh giá xe buýt này rồi");
        }

        // Check if user has actually used this bus (has confirmed ticket)
        boolean hasUsedBus = ticketRepository.existsByUserIdAndSeatBusIdAndStatus(
                userId, request.getBusId(), TicketStatus.CONFIRMED);

        if (!hasUsedBus) {
            throw new IllegalArgumentException("Bạn chỉ có thể đánh giá xe buýt mà bạn đã sử dụng");
        }

        // Create review
        BusReview review = new BusReview();
        review.setBus(bus);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setReview(request.getReview());

        BusReview savedReview = busReviewRepository.save(review);
        log.info("Created bus review: {} for bus: {} by user: {}",
                savedReview.getId(), request.getBusId(), userId);

        return mapToResponse(savedReview, userId);
    }

    public Page<BusReviewResponse> getReviewsByBus(Integer busId, Pageable pageable, Integer currentUserId) {
        // Verify bus exists
        busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe buýt không tồn tại"));

        return busReviewRepository.findByBusIdOrderByCreatedAtDesc(busId, pageable)
                .map(review -> mapToResponse(review, currentUserId));
    }

    public Page<BusReviewResponse> getReviewsByUser(Integer userId, Pageable pageable, Integer currentUserId) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return busReviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(review -> mapToResponse(review, currentUserId));
    }

    public BusReviewStatsResponse getBusReviewStats(Integer busId) {
        // Verify bus exists
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe buýt không tồn tại"));

        BusReviewStatsResponse stats = new BusReviewStatsResponse();
        stats.setBusId(busId);
        stats.setBusLicensePlate(bus.getLicensePlate());

        // Get basic stats
        long totalReviews = busReviewRepository.countReviewsByBusId(busId);
        stats.setTotalReviews((int) totalReviews);

        if (totalReviews == 0) {
            // No reviews yet
            stats.setAverageRating(BigDecimal.ZERO);
            stats.setRatingDistribution(new HashMap<>());
            return stats;
        }

        // Get average rating
        BigDecimal averageRating = busReviewRepository.getAverageRatingByBusId(busId);
        stats.setAverageRating(
                averageRating != null ? averageRating.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // Get rating distribution
        Map<Integer, Integer> distribution = new HashMap<>();
        List<Object[]> ratingCounts = busReviewRepository.getRatingDistributionByBusId(busId);

        for (Object[] row : ratingCounts) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(rating, count.intValue());
        }

        // Fill missing ratings with 0
        for (int i = 1; i <= 5; i++) {
            distribution.putIfAbsent(i, 0);
        }

        stats.setRatingDistribution(distribution);

        // Set individual counts and percentages
        stats.setFiveStarCount(distribution.get(5));
        stats.setFourStarCount(distribution.get(4));
        stats.setThreeStarCount(distribution.get(3));
        stats.setTwoStarCount(distribution.get(2));
        stats.setOneStarCount(distribution.get(1));

        // Calculate percentages
        BigDecimal total = BigDecimal.valueOf(totalReviews);
        stats.setFiveStarPercentage(BigDecimal.valueOf(distribution.get(5)).multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP));
        stats.setFourStarPercentage(BigDecimal.valueOf(distribution.get(4)).multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP));
        stats.setThreeStarPercentage(BigDecimal.valueOf(distribution.get(3)).multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP));
        stats.setTwoStarPercentage(BigDecimal.valueOf(distribution.get(2)).multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP));
        stats.setOneStarPercentage(BigDecimal.valueOf(distribution.get(1)).multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP));

        return stats;
    }

    @Transactional
    public BusReviewResponse updateReview(Integer reviewId, BusReviewUpdateRequest request, Integer userId) {
        BusReview review = busReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        // Check ownership
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền sửa đánh giá này");
        }

        // Update fields if provided
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getReview() != null) {
            review.setReview(request.getReview());
        }

        BusReview updatedReview = busReviewRepository.save(review);
        log.info("Updated bus review: {} by user: {}", reviewId, userId);

        return mapToResponse(updatedReview, userId);
    }

@Transactional
    public void deleteReview(Integer reviewId, Integer userId) {
        BusReview review = busReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        // Check ownership or admin permission
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isOwner = review.getUser().getId().equals(userId);
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Bạn không có quyền xóa đánh giá này");
        }

        busReviewRepository.delete(review);
        log.info("Deleted bus review: {} by user: {}", reviewId, userId);
    }

    public BusReviewResponse getUserReviewForBus(Integer busId, Integer userId) {
        return busReviewRepository.findByBusIdAndUserId(busId, userId)
                .map(review -> mapToResponse(review, userId))
                .orElse(null);
    }

    public Page<BusReviewResponse> getReviewsByCompany(Integer companyId, Pageable pageable, Integer currentUserId) {
        return busReviewRepository.findByBusCompanyIdOrderByCreatedAtDesc(companyId, pageable)
                .map(review -> mapToResponse(review, currentUserId));
    }

    public Page<BusReviewResponse> getReviewsByRating(Integer minRating, Integer maxRating, 
                                                    Pageable pageable, Integer currentUserId) {
        Page<BusReview> reviews;
        
        if (minRating != null && maxRating != null) {
            // Custom query needed for range
            reviews = busReviewRepository.findAll(pageable);
        } else if (minRating != null) {
            reviews = busReviewRepository.findByRatingGreaterThanEqualOrderByCreatedAtDesc(minRating, pageable);
        } else if (maxRating != null) {
            reviews = busReviewRepository.findByRatingLessThanEqualOrderByCreatedAtDesc(maxRating, pageable);
        } else {
            reviews = busReviewRepository.findAll(pageable);
        }

        return reviews.map(review -> mapToResponse(review, currentUserId));
    }

    private BusReviewResponse mapToResponse(BusReview review, Integer currentUserId) {
        BusReviewResponse response = new BusReviewResponse();
        response.setId(review.getId());
        response.setBusId(review.getBus().getId());
        response.setBusLicensePlate(review.getBus().getLicensePlate());
        
        if (review.getBus().getBusCompany() != null) {
            response.setBusCompanyName(review.getBus().getBusCompany().getCompanyName());
        }
        
        response.setRating(review.getRating());
        response.setReview(review.getReview());
        response.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
        response.setUserEmail(review.getUser().getEmail());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());

        // Set permissions
        if (currentUserId != null) {
            boolean isOwner = review.getUser().getId().equals(currentUserId);
            response.setCanEdit(isOwner);
            
            // Check if current user is admin
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            boolean isAdmin = currentUser != null && currentUser.getRoles().stream()
                    .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);
            
            response.setCanDelete(isOwner || isAdmin);
        } else {
            response.setCanEdit(false);
            response.setCanDelete(false);
        }

        return response;
    }
}