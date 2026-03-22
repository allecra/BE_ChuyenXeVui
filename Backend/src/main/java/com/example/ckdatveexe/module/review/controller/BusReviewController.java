package com.example.ckdatveexe.module.review.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.auth.dto.ApiResponse;
import com.example.ckdatveexe.module.review.dto.*;
import com.example.ckdatveexe.module.review.service.BusReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Bus Review Management", description = "APIs for managing bus reviews and ratings")
@Slf4j
public class BusReviewController {

    private final BusReviewService busReviewService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create bus review", description = "Create a new review for a bus (only for users who have used the bus)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> createReview(
            @Valid @RequestBody BusReviewCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            BusReviewResponse response = busReviewService.createReview(request, userDetails.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo đánh giá thành công", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error creating review", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tạo đánh giá"));
        }
    }

    @GetMapping("/bus/{busId}")
    @Operation(summary = "Get reviews by bus", description = "Get paginated list of reviews for a specific bus")
    public ResponseEntity<ApiResponse> getReviewsByBus(
            @Parameter(description = "Bus ID") @PathVariable Integer busId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Integer currentUserId = userDetails != null ? userDetails.getId() : null;
        Page<BusReviewResponse> response = busReviewService.getReviewsByBus(busId, pageable, currentUserId);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công", response));
    }

    @GetMapping("/bus/{busId}/stats")
    @Operation(summary = "Get bus review statistics", description = "Get rating statistics and distribution for a specific bus")
    public ResponseEntity<ApiResponse> getBusReviewStats(
            @Parameter(description = "Bus ID") @PathVariable Integer busId) {

        BusReviewStatsResponse response = busReviewService.getBusReviewStats(busId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê đánh giá thành công", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews by user", description = "Get paginated list of reviews created by a specific user")
    public ResponseEntity<ApiResponse> getReviewsByUser(
            @Parameter(description = "User ID") @PathVariable Integer userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Integer currentUserId = userDetails != null ? userDetails.getId() : null;
        Page<BusReviewResponse> response = busReviewService.getReviewsByUser(userId, pageable, currentUserId);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá của user thành công", response));
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get current user's reviews", description = "Get paginated list of reviews created by current user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> getMyReviews(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BusReviewResponse> response = busReviewService.getReviewsByUser(userDetails.getId(), pageable,
                userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá của bạn thành công", response));
    }

    @GetMapping("/bus/{busId}/my-review")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get current user's review for a bus", description = "Get current user's review for a specific bus (if exists)", security = @SecurityRequirement(name = "bearerAuth"))

    public ResponseEntity<ApiResponse> getMyReviewForBus(
            @Parameter(description = "Bus ID") @PathVariable Integer busId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        BusReviewResponse response = busReviewService.getUserReviewForBus(busId, userDetails.getId());

        if (response != null) {
            return ResponseEntity.ok(ApiResponse.success("Lấy đánh giá thành công", response));
        } else {
            return ResponseEntity.ok(ApiResponse.success("Bạn chưa đánh giá xe buýt này"));
        }
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update review", description = "Update an existing review (only by review owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> updateReview(
            @Parameter(description = "Review ID") @PathVariable Integer reviewId,
            @Valid @RequestBody BusReviewUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            BusReviewResponse response = busReviewService.updateReview(reviewId, request, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Cập nhật đánh giá thành công", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error updating review", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể cập nhật đánh giá"));
        }
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Delete review", description = "Delete a review (by owner or admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> deleteReview(
            @Parameter(description = "Review ID") @PathVariable Integer reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            busReviewService.deleteReview(reviewId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error deleting review", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể xóa đánh giá"));
        }
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get reviews by bus company", description = "Get paginated list of reviews for all buses of a company")
    public ResponseEntity<ApiResponse> getReviewsByCompany(
            @Parameter(description = "Bus Company ID") @PathVariable Integer companyId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Integer currentUserId = userDetails != null ? userDetails.getId() : null;
        Page<BusReviewResponse> response = busReviewService.getReviewsByCompany(companyId, pageable, currentUserId);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá của công ty thành công", response));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter reviews by rating", description = "Get reviews filtered by rating range")
    public ResponseEntity<ApiResponse> getReviewsByRating(
            @Parameter(description = "Minimum rating (1-5)") @RequestParam(required = false) Integer minRating,
            @Parameter(description = "Maximum rating (1-5)") @RequestParam(required = false) Integer maxRating,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Integer currentUserId = userDetails != null ? userDetails.getId() : null;
        Page<BusReviewResponse> response = busReviewService.getReviewsByRating(minRating, maxRating, pageable,
                currentUserId);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá theo rating thành công", response));
    }
}