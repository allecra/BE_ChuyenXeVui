package com.example.ckdatveexe.module.review.controller;

import com.example.ckdatveexe.module.review.dto.ReviewResponse;
import com.example.ckdatveexe.module.review.service.ReviewService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.ReviewStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Company", description = "APIs xem đánh giá cho nhà xe")
public class ReviewCompanyController {

    private final ReviewService reviewService;

    @GetMapping
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy danh sách đánh giá của công ty", description = "Lấy danh sách đánh giá cho các chuyến xe của công ty")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getCompanyReviews(
            @Parameter(description = "Trạng thái đánh giá") @RequestParam(required = false) ReviewStatus status,
            @Parameter(description = "Số sao đánh giá") @RequestParam(required = false) Integer rating,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        try {
            Integer companyId = Integer.valueOf(authentication.getName());
            log.info("🏢 [GET] /api/company/reviews - Company: {}, Status: {}, Rating: {}", companyId, status, rating);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ReviewResponse> reviews = reviewService.getCompanyReviews(companyId, status, rating, pageable);

            log.info("✅ [GET] /api/company/reviews - Success: {} reviews found", reviews.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách đánh giá thành công",
                    reviews));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/reviews - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy thông tin chi tiết đánh giá", description = "Lấy thông tin chi tiết của một đánh giá")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "ID đánh giá") @PathVariable Integer id,
            Authentication authentication) {

        try {
            Integer companyId = Integer.valueOf(authentication.getName());
            log.info("🏢 [GET] /api/company/reviews/{} - Company: {}", id, companyId);

            ReviewResponse review = reviewService.getCompanyReviewById(id, companyId);

            log.info("✅ [GET] /api/company/reviews/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin đánh giá thành công",
                    review));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/reviews/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Thống kê đánh giá của công ty", description = "Lấy thống kê tổng quan về đánh giá của công ty")
    public ResponseEntity<ApiResponse<Object>> getCompanyReviewStatistics(
            Authentication authentication) {

        try {
            Integer companyId = Integer.valueOf(authentication.getName());
            log.info("🏢 [GET] /api/company/reviews/statistics - Company: {}", companyId);

            Double averageRating = reviewService.getCompanyAverageRating(companyId);
            List<Object[]> ratingDistribution = reviewService.getCompanyRatingDistribution(companyId);
            long totalReviews = reviewService.getCompanyReviewCount(companyId, null);
            long approvedReviews = reviewService.getCompanyReviewCount(companyId, ReviewStatus.APPROVED);
            long pendingReviews = reviewService.getCompanyReviewCount(companyId, ReviewStatus.PENDING);
            long rejectedReviews = reviewService.getCompanyReviewCount(companyId, ReviewStatus.REJECTED);

            // Create statistics object
            var statistics = java.util.Map.of(
                    "averageRating", averageRating,
                    "totalReviews", totalReviews,
                    "approvedReviews", approvedReviews,
                    "pendingReviews", pendingReviews,
                    "rejectedReviews", rejectedReviews,
                    "ratingDistribution", ratingDistribution);

            log.info("✅ [GET] /api/company/reviews/statistics - Success: Avg rating {}", averageRating);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thống kê đánh giá thành công",
                    statistics));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/reviews/statistics - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/average-rating")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy điểm đánh giá trung bình", description = "Lấy điểm đánh giá trung bình của công ty")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            Authentication authentication) {

        try {
            Integer companyId = Integer.valueOf(authentication.getName());
            log.info("🏢 [GET] /api/company/reviews/average-rating - Company: {}", companyId);

            Double averageRating = reviewService.getCompanyAverageRating(companyId);

            log.info("✅ [GET] /api/company/reviews/average-rating - Success: {}", averageRating);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy điểm đánh giá trung bình thành công",
                    averageRating));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/reviews/average-rating - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}