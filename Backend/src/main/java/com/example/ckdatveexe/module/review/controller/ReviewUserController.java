package com.example.ckdatveexe.module.review.controller;

import com.example.ckdatveexe.module.review.dto.*;
import com.example.ckdatveexe.module.review.service.ReviewService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
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
@RequestMapping("/api/user/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review User", description = "APIs đánh giá cho người dùng")
public class ReviewUserController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Tạo đánh giá mới", description = "Tạo đánh giá cho chuyến xe đã hoàn thành")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {

        try {
            Integer userId = Integer.valueOf(authentication.getName());
            log.info("👤 [POST] /api/user/reviews - User: {} creating review for ticket: {}", userId,
                    request.getTicketId());

            ReviewResponse review = reviewService.createReview(request, userId);

            log.info("✅ [POST] /api/user/reviews - Success: Review created with ID {}", review.getId());
            return ResponseEntity.ok(ApiResponse.success(
                    "Tạo đánh giá thành công",
                    review));
        } catch (Exception e) {
            log.error("💥 [POST] /api/user/reviews - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy danh sách đánh giá của tôi", description = "Lấy danh sách đánh giá đã tạo")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        try {
            Integer userId = Integer.valueOf(authentication.getName());
            log.info("👤 [GET] /api/user/reviews - User: {}, Page: {}", userId, page);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ReviewResponse> reviews = reviewService.getUserReviews(userId, pageable);

            log.info("✅ [GET] /api/user/reviews - Success: {} reviews found", reviews.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách đánh giá thành công",
                    reviews));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/reviews - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy thông tin chi tiết đánh giá", description = "Lấy thông tin chi tiết của một đánh giá")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "ID đánh giá") @PathVariable Integer id,
            Authentication authentication) {

        try {
            Integer userId = Integer.valueOf(authentication.getName());
            log.info("👤 [GET] /api/user/reviews/{} - User: {}", id, userId);

            ReviewResponse review = reviewService.getReviewById(id, userId);

            log.info("✅ [GET] /api/user/reviews/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin đánh giá thành công",
                    review));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/reviews/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/can-review/{ticketId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Kiểm tra có thể đánh giá vé", description = "Kiểm tra xem có thể tạo đánh giá cho vé hay không")
    public ResponseEntity<ApiResponse<Boolean>> canReviewTicket(
            @Parameter(description = "ID vé") @PathVariable Integer ticketId,
            Authentication authentication) {

        try {
            Integer userId = Integer.valueOf(authentication.getName());
            log.info("👤 [GET] /api/user/reviews/can-review/{} - User: {}", ticketId, userId);

            boolean canReview = reviewService.canUserReviewTicket(userId, ticketId);

            log.info("✅ [GET] /api/user/reviews/can-review/{} - Can review: {}", ticketId, canReview);
            return ResponseEntity.ok(ApiResponse.success(
                    canReview ? "Có thể tạo đánh giá" : "Không thể tạo đánh giá",
                    canReview));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/reviews/can-review/{} - Error: {}", ticketId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}