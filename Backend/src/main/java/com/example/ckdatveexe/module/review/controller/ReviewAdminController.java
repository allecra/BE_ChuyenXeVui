package com.example.ckdatveexe.module.review.controller;

import com.example.ckdatveexe.module.review.dto.*;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Admin", description = "APIs quản lý đánh giá cho admin")
public class ReviewAdminController {

    private final ReviewService reviewService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách tất cả đánh giá", description = "Lấy danh sách tất cả đánh giá trong hệ thống với phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAllReviews(
            @Parameter(description = "Trạng thái đánh giá") @RequestParam(required = false) ReviewStatus status,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID nhà xe") @RequestParam(required = false) Integer companyId,
            @Parameter(description = "Số sao đánh giá") @RequestParam(required = false) Integer rating,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            log.info("👑 [GET] /api/admin/reviews - Status: {}, Keyword: {}, Company: {}, Rating: {}",
                    status, keyword, companyId, rating);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ReviewResponse> reviews = reviewService.getAllReviews(status, keyword, companyId, rating, pageable);

            log.info("✅ [GET] /api/admin/reviews - Success: {} reviews found", reviews.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách đánh giá thành công",
                    reviews));
        } catch (Exception e) {
            log.error("💥 [GET] /api/admin/reviews - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách đánh giá chờ duyệt", description = "Lấy danh sách đánh giá đang chờ duyệt")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getPendingReviews(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size) {

        try {
            log.info("👑 [GET] /api/admin/reviews/pending - Page: {}", page);

            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewResponse> reviews = reviewService.getPendingReviews(pageable);

            log.info("✅ [GET] /api/admin/reviews/pending - Success: {} pending reviews", reviews.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách đánh giá chờ duyệt thành công",
                    reviews));
        } catch (Exception e) {
            log.error("💥 [GET] /api/admin/reviews/pending - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy thông tin chi tiết đánh giá", description = "Lấy thông tin chi tiết của một đánh giá")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "ID đánh giá") @PathVariable Integer id) {

        try {
            log.info("👑 [GET] /api/admin/reviews/{} - Getting review details", id);

            ReviewResponse review = reviewService.getAdminReviewById(id);

            log.info("✅ [GET] /api/admin/reviews/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin đánh giá thành công",
                    review));
        } catch (Exception e) {
            log.error("💥 [GET] /api/admin/reviews/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt đánh giá", description = "Duyệt đánh giá để hiển thị công khai")
    public ResponseEntity<ApiResponse<ReviewResponse>> approveReview(
            @Parameter(description = "ID đánh giá") @PathVariable Integer id,
            @Valid @RequestBody(required = false) ReviewActionRequest request,
            Authentication authentication) {

        try {
            Integer adminId = Integer.valueOf(authentication.getName());
            log.info("👑 [POST] /api/admin/reviews/{}/approve - Admin: {}", id, adminId);

            if (request == null) {
                request = new ReviewActionRequest();
            }

            ReviewResponse review = reviewService.approveReview(id, request, adminId);

            log.info("✅ [POST] /api/admin/reviews/{}/approve - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Duyệt đánh giá thành công",
                    review));
        } catch (Exception e) {
            log.error("💥 [POST] /api/admin/reviews/{}/approve - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Từ chối đánh giá", description = "Từ chối đánh giá và không hiển thị công khai")
    public ResponseEntity<ApiResponse<ReviewResponse>> rejectReview(
            @Parameter(description = "ID đánh giá") @PathVariable Integer id,
            @Valid @RequestBody ReviewActionRequest request,
            Authentication authentication) {

        try {
            Integer adminId = Integer.valueOf(authentication.getName());
            log.info("👑 [POST] /api/admin/reviews/{}/reject - Admin: {}", id, adminId);

            ReviewResponse review = reviewService.rejectReview(id, request, adminId);

            log.info("✅ [POST] /api/admin/reviews/{}/reject - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Từ chối đánh giá thành công",
                    review));
        } catch (Exception e) {
            log.error("💥 [POST] /api/admin/reviews/{}/reject - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê đánh giá tổng quan", description = "Lấy thống kê tổng quan về đánh giá trong hệ thống")
    public ResponseEntity<ApiResponse<Object>> getReviewStatistics() {

        try {
            log.info("👑 [GET] /api/admin/reviews/statistics - Getting review statistics");

            // Create statistics object
            long totalReviews = reviewService.getReviewCountByStatus(null);
            long approvedReviews = reviewService.getReviewCountByStatus(ReviewStatus.APPROVED);
            long pendingReviews = reviewService.getReviewCountByStatus(ReviewStatus.PENDING);
            long rejectedReviews = reviewService.getReviewCountByStatus(ReviewStatus.REJECTED);

            var statistics = java.util.Map.of(
                    "totalReviews", totalReviews,
                    "approvedReviews", approvedReviews,
                    "pendingReviews", pendingReviews,
                    "rejectedReviews", rejectedReviews);

            log.info("✅ [GET] /api/admin/reviews/statistics - Success");
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thống kê đánh giá thành công",
                    statistics));
        } catch (Exception e) {
            log.error("💥 [GET] /api/admin/reviews/statistics - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}