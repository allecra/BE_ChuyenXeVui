package com.example.ckdatveexe.module.payment.controller;

import com.example.ckdatveexe.module.payment.dto.PaymentResponse;
import com.example.ckdatveexe.module.payment.provider.PaymentProviderInterface;
import com.example.ckdatveexe.module.payment.service.PaymentService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payment Public API", description = "API thanh toán công khai")
@SecurityRequirements() // No authentication required
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final List<PaymentProviderInterface> paymentProviders;

    @GetMapping("/test/providers")
    @Operation(summary = "Test payment providers", description = "Kiểm tra trạng thái các nhà cung cấp thanh toán")
    public ResponseEntity<Map<String, Object>> testProviders() {
        log.info("🧪 [TEST] Testing payment providers");

        Map<String, Object> result = new HashMap<>();

        // Test all available providers
        for (PaymentProviderInterface provider : paymentProviders) {
            String providerName = provider.getProviderName().toLowerCase();
            try {
                result.put(providerName + "_enabled", provider.isEnabled());
                result.put(providerName + "_name", provider.getProviderName());
                log.info("🧪 [TEST] Provider {} - Enabled: {}", provider.getProviderName(), provider.isEnabled());
            } catch (Exception e) {
                result.put(providerName + "_error", e.getMessage());
                log.error("🧪 [TEST] Provider {} - Error: {}", provider.getProviderName(), e.getMessage());
            }
        }

        result.put("total_providers", paymentProviders.size());
        result.put("test_time", java.time.LocalDateTime.now());

        log.info("✅ [TEST] Provider test completed - {} providers found", paymentProviders.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/test/cleanup")
    @Operation(summary = "Test cleanup expired payments", description = "Trigger cleanup expired payments manually")
    public ResponseEntity<Map<String, Object>> testCleanup() {
        log.info("🧪 [TEST] Manual cleanup trigger");

        try {
            paymentService.cancelExpiredPayments();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Cleanup completed successfully");
            result.put("cleanup_time", java.time.LocalDateTime.now());

            log.info("✅ [TEST] Manual cleanup completed successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("💥 [TEST] Manual cleanup failed", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/status/{transactionId}")
    @Operation(summary = "Kiểm tra trạng thái thanh toán", description = "Lấy thông tin trạng thái thanh toán theo mã giao dịch")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(@PathVariable String transactionId) {

        log.info("💳 [PUBLIC] GET /api/payment/status/{} - Get payment status", transactionId);

        try {
            PaymentResponse payment = paymentService.getPaymentStatus(transactionId);

            log.info("✅ [PUBLIC] 200 OK - Retrieved payment status for transaction: {}", transactionId);
            return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                    .success(true)
                    .message("Lấy trạng thái thanh toán thành công")
                    .data(payment)
                    .build());

        } catch (Exception e) {
            log.error("💥 [PUBLIC] 404 NOT_FOUND - Payment not found: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PaymentResponse>builder()
                            .success(false)
                            .message("Không tìm thấy giao dịch thanh toán")
                            .build());
        }
    }

    @PostMapping("/ipn/momo")
    @Operation(summary = "Nhận callback từ MoMo", description = "Endpoint nhận thông báo thanh toán từ MoMo")
    public ResponseEntity<Map<String, Object>> handleMoMoCallback(@RequestBody Map<String, Object> callback) {

        log.info("💳 [MOMO-IPN] POST /api/payment/ipn/momo - Received callback");

        try {
            boolean success = paymentService.processCallback("MOMO", callback);

            if (success) {
                log.info("✅ [MOMO-IPN] 200 OK - Callback processed successfully");
                return ResponseEntity.ok(Map.of(
                        "resultCode", 0,
                        "message", "Success"));
            } else {
                log.warn("⚠️ [MOMO-IPN] 400 BAD_REQUEST - Invalid callback");
                return ResponseEntity.badRequest().body(Map.of(
                        "resultCode", 1,
                        "message", "Invalid callback"));
            }

        } catch (Exception e) {
            log.error("💥 [MOMO-IPN] 500 INTERNAL_SERVER_ERROR - Error processing callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "resultCode", 1,
                            "message", "Internal server error"));
        }
    }

    @PostMapping("/ipn/sepay")
    @Operation(summary = "Nhận callback từ SePay", description = "Endpoint nhận thông báo thanh toán từ SePay")
    public ResponseEntity<Map<String, Object>> handleSePayCallback(@RequestBody Map<String, Object> callback) {

        log.info("💳 [SEPAY-IPN] POST /api/payment/ipn/sepay - Received callback");

        try {
            boolean success = paymentService.processCallback("SEPAY", callback);

            if (success) {
                log.info("✅ [SEPAY-IPN] 200 OK - Callback processed successfully");
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Callback processed successfully"));
            } else {
                log.warn("⚠️ [SEPAY-IPN] 400 BAD_REQUEST - Invalid callback");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid callback"));
            }

        } catch (Exception e) {
            log.error("💥 [SEPAY-IPN] 500 INTERNAL_SERVER_ERROR - Error processing callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Internal server error"));
        }
    }
}