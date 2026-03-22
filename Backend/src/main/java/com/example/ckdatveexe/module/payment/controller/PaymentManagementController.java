package com.example.ckdatveexe.module.payment.controller;

import com.example.ckdatveexe.module.auth.dto.ApiResponse;
import com.example.ckdatveexe.module.payment.dto.*;
import com.example.ckdatveexe.module.payment.service.PaymentManagementService;
import com.example.ckdatveexe.shared.entity.PaymentStatus;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for payment processing and management")
@Slf4j
public class PaymentManagementController {

    private final PaymentManagementService paymentManagementService;

    @PostMapping("/sepay/callback")
    @Operation(summary = "SePay callback handler", description = "Handle SePay payment callback notifications")
    public ResponseEntity<ApiResponse> handleSepayCallback(
            @Valid @RequestBody SepayCallbackRequest request) {

        try {
            PaymentResponse response = paymentManagementService.processSepayCallback(request);
            return ResponseEntity.ok(ApiResponse.success("SePay callback processed successfully", response));

        } catch (IllegalArgumentException e) {
            log.error("SePay callback validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error processing SePay callback", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to process SePay callback"));
        }
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Process refund", description = "Process a refund for a payment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> processRefund(
            @Valid @RequestBody RefundProcessRequest request) {

        try {
            PaymentResponse response = paymentManagementService.processRefund(request);
            return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error processing refund", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to process refund"));
        }
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all payments", description = "Get paginated list of all payments (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> getAllPayments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentResponse> response = paymentManagementService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.success("Retrieved payments successfully", response));
    }

    @GetMapping("/admin/payments/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payments by status", description = "Get paginated list of payments filtered by status", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentResponse> response = paymentManagementService.getPaymentsByStatus(status, pageable);

        return ResponseEntity.ok(ApiResponse.success("Retrieved payments by status successfully", response));
    }

    @GetMapping("/admin/payments/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payments by date range", description = "Get paginated list of payments within a date range", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> getPaymentsByDateRange(
            @Parameter(description = "From date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "To date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentResponse> response = paymentManagementService.getPaymentsByDateRange(fromDate, toDate, pageable);

        return ResponseEntity.ok(ApiResponse.success("Retrieved payments by date range successfully", response));
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate payment report", description = "Generate comprehensive payment statistics and revenue report", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> generatePaymentReport(
            @Parameter(description = "From date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "To date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        PaymentReportResponse response = paymentManagementService.generatePaymentReport(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Payment report generated successfully", response));
    }

    @GetMapping("/admin/refunds/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending refunds", description = "Get list of all pending refunds", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> getPendingRefunds() {

        List<PaymentResponse> response = paymentManagementService.getPendingRefunds();
        return ResponseEntity.ok(ApiResponse.success("Retrieved pending refunds successfully", response));
    }

    @PutMapping("/admin/refunds/{refundId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark refund as completed", description = "Mark a pending refund as completed", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> markRefundAsCompleted(
            @Parameter(description = "Refund payment ID") @PathVariable Integer refundId) {

        try {
            paymentManagementService.markRefundAsCompleted(refundId);
            return ResponseEntity.ok(ApiResponse.success("Refund marked as completed successfully", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error marking refund as completed", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to mark refund as completed"));
        }
    }
}