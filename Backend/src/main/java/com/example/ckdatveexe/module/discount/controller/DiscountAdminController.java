package com.example.ckdatveexe.module.discount.controller;

import com.example.ckdatveexe.module.discount.dto.*;
import com.example.ckdatveexe.module.discount.service.DiscountService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.DiscountScope;
import com.example.ckdatveexe.shared.entity.DiscountStatus;
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
import java.util.List;

@RestController
@RequestMapping("/api/admin/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discount Admin", description = "APIs quản lý mã giảm giá cho admin")
public class DiscountAdminController {

    private final DiscountService discountService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách tất cả mã giảm giá", description = "Lấy danh sách tất cả mã giảm giá trong hệ thống với phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<Page<DiscountCodeResponse>>> getAllDiscounts(
            @Parameter(description = "Trạng thái mã giảm giá") @RequestParam(required = false) DiscountStatus status,
            @Parameter(description = "Phạm vi áp dụng") @RequestParam(required = false) DiscountScope scope,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID nhà xe") @RequestParam(required = false) Integer companyId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            log.info("👑 [GET] /api/admin/discounts - Status: {}, Scope: {}, Keyword: {}, Company: {}",
                    status, scope, keyword, companyId);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DiscountCodeResponse> discounts = discountService.getDiscounts(status, scope, keyword, companyId,
                    pageable);

            log.info("✅ [GET] /api/admin/discounts - Success: {} discounts found", discounts.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách mã giảm giá thành công",
                    discounts));
        } catch (Exception e) {
            log.error("💥 [GET] /api/admin/discounts - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy thông tin chi tiết mã giảm giá", description = "Lấy thông tin chi tiết của một mã giảm giá")
    public ResponseEntity<ApiResponse<DiscountCodeResponse>> getDiscountById(
            @Parameter(description = "ID mã giảm giá") @PathVariable Integer id) {

        try {
            log.info("👑 [GET] /api/admin/discounts/{} - Getting discount details", id);

            DiscountCodeResponse discount = discountService.getDiscountById(id);

            log.info("✅ [GET] /api/admin/discounts/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin mã giảm giá thành công",
                    discount));
        } catch (Exception e) {
            log.error("💥 [GET] /api/admin/discounts/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo mã giảm giá mới", description = "Tạo mã giảm giá mới (platform hoặc company)")
    public ResponseEntity<ApiResponse<DiscountCodeResponse>> createDiscount(
            @Valid @RequestBody DiscountCodeCreateRequest request,
            Authentication authentication) {

        try {
            Integer adminId = Integer.valueOf(authentication.getName());
            log.info("👑 [POST] /api/admin/discounts - Creating discount: {} by admin: {}", request.getName(), adminId);

            DiscountCodeResponse discount = discountService.createDiscount(request, adminId);

            log.info("✅ [POST] /api/admin/discounts - Success: Discount created with ID {}", discount.getId());
            return ResponseEntity.ok(ApiResponse.success(
                    "Tạo mã giảm giá thành công",
                    discount));
        } catch (Exception e) {
            log.error("💥 [POST] /api/admin/discounts - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật mã giảm giá", description = "Cập nhật thông tin mã giảm giá")
    public ResponseEntity<ApiResponse<DiscountCodeResponse>> updateDiscount(
            @Parameter(description = "ID mã giảm giá") @PathVariable Integer id,
            @Valid @RequestBody DiscountCodeCreateRequest request) {

        try {
            log.info("👑 [PUT] /api/admin/discounts/{} - Updating discount", id);

            DiscountCodeResponse discount = discountService.updateDiscount(id, request);

            log.info("✅ [PUT] /api/admin/discounts/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Cập nhật mã giảm giá thành công",
                    discount));
        } catch (Exception e) {
            log.error("💥 [PUT] /api/admin/discounts/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa mã giảm giá", description = "Xóa mã giảm giá khỏi hệ thống")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(
            @Parameter(description = "ID mã giảm giá") @PathVariable Integer id) {

        try {
            log.info("👑 [DELETE] /api/admin/discounts/{} - Deleting discount", id);

            discountService.deleteDiscount(id);

            log.info("✅ [DELETE] /api/admin/discounts/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success("Xóa mã giảm giá thành công", null));
        } catch (Exception e) {
            log.error("💥 [DELETE] /api/admin/discounts/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật trạng thái mã giảm giá", description = "Cập nhật trạng thái hoạt động của mã giảm giá")
    public ResponseEntity<ApiResponse<DiscountCodeResponse>> updateDiscountStatus(
            @Parameter(description = "ID mã giảm giá") @PathVariable Integer id,
            @Parameter(description = "Trạng thái mới") @RequestParam DiscountStatus status) {

        try {
            log.info("👑 [PUT] /api/admin/discounts/{}/status - Updating status to {}", id, status);

            DiscountCodeResponse discount = discountService.updateDiscountStatus(id, status);

            log.info("✅ [PUT] /api/admin/discounts/{}/status - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Cập nhật trạng thái mã giảm giá thành công",
                    discount));
        } catch (Exception e) {
            log.error("💥 [PUT] /api/admin/discounts/{}/status - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê mã giảm giá", description = "Lấy thống kê tổng quan về mã giảm giá")
    public ResponseEntity<ApiResponse<Object>> getDiscountStatistics() {

        try {
            log.info("👑 [GET] /api/admin/discounts/statistics - Getting discount statistics");

            // Create statistics object
            long totalDiscounts = discountService.getDiscountCountByStatus(null);
            long activeDiscounts = discountService.getDiscountCountByStatus(DiscountStatus.ACTIVE);
            long expiredDiscounts = discountService.getDiscountCountByStatus(DiscountStatus.EXPIRED);
            long usedUpDiscounts = discountService.getDiscountCountByStatus(DiscountStatus.USED_UP);
            long platformDiscounts = discountService.getDiscountCountByScope(DiscountScope.PLATFORM);
            long companyDiscounts = discountService.getDiscountCountByScope(DiscountScope.COMPANY);
            long routeDiscounts = discountService.getDiscountCountByScope(DiscountScope.ROUTE);

            var statistics = java.util.Map.of(
                    "totalDiscounts", totalDiscounts,
                    "activeDiscounts", activeDiscounts,
                    "expiredDiscounts", expiredDiscounts,
                    "usedUpDiscounts", usedUpDiscounts,
                    "platformDiscounts", platformDiscounts,
                    "companyDiscounts", companyDiscounts,
                    "routeDiscounts", routeDiscounts);

            log.info("✅ [GET] /api/admin/discounts/statistics - Success");
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thống kê mã giảm giá thành công",
                    statistics));
        } catch (Exception e) {
            log.error("💥 [GET] /api/admin/discounts/statistics - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách mã giảm giá sắp hết hạn", description = "Lấy danh sách mã giảm giá sắp hết hạn trong 7 ngày tới")
    public ResponseEntity<ApiResponse<List<DiscountCodeResponse>>> getExpiringDiscounts() {

        try {
            log.info("👑 [GET] /api/admin/discounts/expiring - Getting expiring discounts");

            List<DiscountCodeResponse> discounts = discountService.getExpiringDiscounts();

            log.info("✅ [GET] /api/admin/discounts/expiring - Success: {} discounts found", discounts.size());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách mã giảm giá sắp hết hạn thành công",
                    discounts));
        } catch (Exception e) {
            log.error("💥 [GET] /api/admin/discounts/expiring - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}