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
@RequestMapping("/api/company/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discount Company", description = "APIs quản lý mã giảm giá cho nhà xe")
public class DiscountCompanyController {

    private final DiscountService discountService;

    @GetMapping
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy danh sách mã giảm giá", description = "Lấy danh sách mã giảm giá của công ty với phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<Page<DiscountCodeResponse>>> getDiscounts(
            @Parameter(description = "Trạng thái mã giảm giá") @RequestParam(required = false) DiscountStatus status,
            @Parameter(description = "Phạm vi áp dụng") @RequestParam(required = false) DiscountScope scope,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        try {
            Integer companyId = Integer.valueOf(authentication.getName());
            log.info("🏢 [GET] /api/company/discounts - Company: {}, Status: {}, Keyword: {}", companyId, status,
                    keyword);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DiscountCodeResponse> discounts = discountService.getDiscounts(status, scope, keyword, companyId,
                    pageable);

            log.info("✅ [GET] /api/company/discounts - Success: {} discounts found", discounts.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách mã giảm giá thành công",
                    discounts));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/discounts - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy thông tin chi tiết mã giảm giá", description = "Lấy thông tin chi tiết của một mã giảm giá")
    public ResponseEntity<ApiResponse<DiscountCodeResponse>> getDiscountById(
            @Parameter(description = "ID mã giảm giá") @PathVariable Integer id) {

        try {
            log.info("🏢 [GET] /api/company/discounts/{} - Getting discount details", id);

            DiscountCodeResponse discount = discountService.getDiscountById(id);

            log.info("✅ [GET] /api/company/discounts/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin mã giảm giá thành công",
                    discount));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/discounts/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Tạo mã giảm giá mới", description = "Tạo mã giảm giá mới cho công ty")
    public ResponseEntity<ApiResponse<DiscountCodeResponse>> createDiscount(
            @Valid @RequestBody DiscountCodeCreateRequest request,
            Authentication authentication) {

        try {
            Integer companyId = Integer.valueOf(authentication.getName());
            log.info("🏢 [POST] /api/company/discounts - Creating discount: {} by company: {}", request.getName(),
                    companyId);

            // Ensure company scope for company-created discounts
            if (request.getScope() == DiscountScope.COMPANY) {
                request.setBusCompanyId(companyId);
            }

            DiscountCodeResponse discount = discountService.createDiscount(request, companyId);

            log.info("✅ [POST] /api/company/discounts - Success: Discount created with ID {}", discount.getId());
            return ResponseEntity.ok(ApiResponse.success(
                    "Tạo mã giảm giá thành công",
                    discount));
        } catch (Exception e) {
            log.error("💥 [POST] /api/company/discounts - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Cập nhật mã giảm giá", description = "Cập nhật thông tin mã giảm giá")
    public ResponseEntity<ApiResponse<DiscountCodeResponse>> updateDiscount(
            @Parameter(description = "ID mã giảm giá") @PathVariable Integer id,
            @Valid @RequestBody DiscountCodeCreateRequest request,
            Authentication authentication) {
        
        try {
            Integer companyId = Integer.valueOf(authentication.getName());
            log.info("🏢 [PUT] /api/company/discounts/{} - Updating discount by company: {}", id, companyId);

            // Ensure company scope for company-updated discounts
            if (request.getScope() == DiscountScope.COMPANY) {
                request.setBusCompanyId(companyId);
            }

            DiscountCodeResponse discount = discountService.updateDiscount(id, request);

            log.info("✅ [PUT] /api/company/discounts/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật mã giảm giá thành công", 
                discount));
        } catch (Exception e) {
            log.error("💥 [PUT] /api/company/discounts/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

@DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Xóa mã giảm giá", description = "Xóa mã giảm giá khỏi hệ thống")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(
            @Parameter(description = "ID mã giảm giá") @PathVariable Integer id) {
        
        try {
            log.info("🏢 [DELETE] /api/company/discounts/{} - Deleting discount", id);

            discountService.deleteDiscount(id);

            log.info("✅ [DELETE] /api/company/discounts/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success("Xóa mã giảm giá thành công", null));
        } catch (Exception e) {
            log.error("💥 [DELETE] /api/company/discounts/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Cập nhật trạng thái mã giảm giá", description = "Cập nhật trạng thái hoạt động của mã giảm giá")
    public ResponseEntity<ApiResponse<DiscountCodeResponse>> updateDiscountStatus(
            @Parameter(description = "ID mã giảm giá") @PathVariable Integer id,
            @Parameter(description = "Trạng thái mới") @RequestParam DiscountStatus status) {
        
        try {
            log.info("🏢 [PUT] /api/company/discounts/{}/status - Updating status to {}", id, status);

            DiscountCodeResponse discount = discountService.updateDiscountStatus(id, status);

            log.info("✅ [PUT] /api/company/discounts/{}/status - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái mã giảm giá thành công", 
                discount));
        } catch (Exception e) {
            log.error("💥 [PUT] /api/company/discounts/{}/status - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy danh sách mã giảm giá sắp hết hạn", description = "Lấy danh sách mã giảm giá sắp hết hạn trong 7 ngày tới")
    public ResponseEntity<ApiResponse<List<DiscountCodeResponse>>> getExpiringDiscounts() {
        
        try {
            log.info("🏢 [GET] /api/company/discounts/expiring - Getting expiring discounts");

            List<DiscountCodeResponse> discounts = discountService.getExpiringDiscounts();

            log.info("✅ [GET] /api/company/discounts/expiring - Success: {} discounts found", discounts.size());
            return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách mã giảm giá sắp hết hạn thành công", 
                discounts));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/discounts/expiring - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}