package com.example.ckdatveexe.module.payment.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.payment.dto.PaymentCreateRequest;
import com.example.ckdatveexe.module.payment.dto.PaymentResponse;
import com.example.ckdatveexe.module.payment.service.PaymentService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/payments")
@RequiredArgsConstructor
@Tag(name = "Payment User API", description = "API thanh toán cho người dùng")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class PaymentUserController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Tạo yêu cầu thanh toán", description = "Tạo yêu cầu thanh toán và nhận QR code")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("💳 [USER] POST /api/user/payments - Create payment for user: {}", userDetails.getId());

        try {
            PaymentResponse payment = paymentService.createPayment(request, userDetails.getId());

            log.info("✅ [USER] 201 CREATED - Payment created successfully: {}", payment.getTransactionId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<PaymentResponse>builder()
                            .success(true)
                            .message("Tạo yêu cầu thanh toán thành công")
                            .data(payment)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("💥 [USER] 400 BAD_REQUEST - Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PaymentResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Error creating payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PaymentResponse>builder()
                            .success(false)
                            .message("Không thể tạo yêu cầu thanh toán")
                            .build());
        }
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách thanh toán", description = "Lấy danh sách thanh toán của người dùng")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getUserPayments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("💳 [USER] GET /api/user/payments - Get payments for user: {} (page: {}, size: {})",
                userDetails.getId(), page, size);

        try {
            List<PaymentResponse> payments = paymentService.getUserPayments(userDetails.getId());

            log.info("✅ [USER] 200 OK - Retrieved {} payments for user: {}", payments.size(), userDetails.getId());
            return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách thanh toán thành công")
                    .data(payments)
                    .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Error getting user payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<PaymentResponse>>builder()
                            .success(false)
                            .message("Không thể lấy danh sách thanh toán")
                            .build());
        }
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Lấy chi tiết thanh toán", description = "Lấy thông tin chi tiết thanh toán theo mã giao dịch")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentDetail(
            @PathVariable String transactionId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("💳 [USER] GET /api/user/payments/{} - Get payment detail for user: {}",
                transactionId, userDetails.getId());

        try {
            PaymentResponse payment = paymentService.getPaymentStatus(transactionId);

            // Verify payment belongs to user
            if (!payment.getUserId().equals(userDetails.getId())) {
                log.warn("⚠️ [USER] 403 FORBIDDEN - Payment does not belong to user: {}", userDetails.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<PaymentResponse>builder()
                                .success(false)
                                .message("Không có quyền truy cập thanh toán này")
                                .build());
            }

            log.info("✅ [USER] 200 OK - Retrieved payment detail: {}", transactionId);
            return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                    .success(true)
                    .message("Lấy chi tiết thanh toán thành công")
                    .data(payment)
                    .build());

        } catch (Exception e) {
            log.error("💥 [USER] 404 NOT_FOUND - Payment not found: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PaymentResponse>builder()
                            .success(false)
                            .message("Không tìm thấy thanh toán")
                            .build());
        }
    }

    @PostMapping("/{transactionId}/cancel")
    @Operation(summary = "Hủy thanh toán", description = "Hủy yêu cầu thanh toán đang chờ xử lý")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable String transactionId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("💳 [USER] POST /api/user/payments/{}/cancel - Cancel payment for user: {}",
                transactionId, userDetails.getId());

        try {
            PaymentResponse payment = paymentService.getPaymentStatus(transactionId);

            // Verify payment belongs to user
            if (!payment.getUserId().equals(userDetails.getId())) {
                log.warn("⚠️ [USER] 403 FORBIDDEN - Payment does not belong to user: {}", userDetails.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<PaymentResponse>builder()
                                .success(false)
                                .message("Không có quyền hủy thanh toán này")
                                .build());
            }

            // Check if payment can be cancelled
            if (!"PENDING".equals(payment.getStatus())) {
                log.warn("⚠️ [USER] 400 BAD_REQUEST - Payment cannot be cancelled, status: {}", payment.getStatus());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<PaymentResponse>builder()
                                .success(false)
                                .message("Không thể hủy thanh toán với trạng thái hiện tại")
                                .build());
            }

            // TODO: Implement cancel payment logic in service
            log.info("✅ [USER] 200 OK - Payment cancellation requested: {}", transactionId);
            return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                    .success(true)
                    .message("Yêu cầu hủy thanh toán đã được ghi nhận")
                    .data(payment)
                    .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Error cancelling payment: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PaymentResponse>builder()
                            .success(false)
                            .message("Không thể hủy thanh toán")
                            .build());
        }
    }
}