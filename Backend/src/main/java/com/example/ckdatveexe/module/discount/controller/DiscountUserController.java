package com.example.ckdatveexe.module.discount.controller;

import com.example.ckdatveexe.module.discount.dto.*;
import com.example.ckdatveexe.module.discount.service.DiscountService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/user/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discount User", description = "APIs mã giảm giá cho người dùng")
public class DiscountUserController {

    private final DiscountService discountService;

    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy danh sách mã giảm giá đang hoạt động", description = "Lấy danh sách mã giảm giá có thể sử dụng")
    public ResponseEntity<ApiResponse<List<DiscountCodeResponse>>> getActiveDiscounts(
            @Parameter(description = "ID nhà xe (tùy chọn)") @RequestParam(required = false) Integer companyId) {

        try {
            log.info("👤 [GET] /api/user/discounts/active - Company: {}", companyId);

            List<DiscountCodeResponse> discounts = discountService.getActiveDiscounts(companyId);

            log.info("✅ [GET] /api/user/discounts/active - Success: {} discounts found", discounts.size());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách mã giảm giá thành công",
                    discounts));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/discounts/active - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy thông tin mã giảm giá", description = "Lấy thông tin chi tiết của mã giảm giá")
    public ResponseEntity<ApiResponse<DiscountCodeResponse>> getDiscountByCode(
            @Parameter(description = "Mã giảm giá") @PathVariable String code) {

        try {
            log.info("👤 [GET] /api/user/discounts/{} - Getting discount details", code);

            DiscountCodeResponse discount = discountService.getDiscountByCode(code);

            log.info("✅ [GET] /api/user/discounts/{} - Success", code);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin mã giảm giá thành công",
                    discount));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/discounts/{} - Error: {}", code, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Áp dụng mã giảm giá", description = "Kiểm tra và áp dụng mã giảm giá cho đơn hàng")
    public ResponseEntity<ApiResponse<ApplyDiscountResponse>> applyDiscount(
            @Valid @RequestBody ApplyDiscountRequest request,
            Authentication authentication) {

        try {
            Integer userId = Integer.valueOf(authentication.getName());
            log.info("👤 [POST] /api/user/discounts/apply - User: {}, Code: {}", userId, request.getDiscountCode());

            ApplyDiscountResponse response = discountService.applyDiscount(request, userId);

            if (response.isValid()) {
                log.info("✅ [POST] /api/user/discounts/apply - Success: {} VND discount", response.getDiscountAmount());
                return ResponseEntity.ok(ApiResponse.success(
                        "Áp dụng mã giảm giá thành công",
                        response));
            } else {
                log.warn("⚠️ [POST] /api/user/discounts/apply - Invalid: {}", response.getMessage());
                return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
            }
        } catch (Exception e) {
            log.error("💥 [POST] /api/user/discounts/apply - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}