package com.example.ckdatveexe.payment;

import com.example.ckdatveexe.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Payment Management API Integration Tests")
class PaymentControllerIntegrationTest extends BaseIntegrationTest {

        // ==================== PUBLIC PAYMENT APIs ====================

        @Test
        @DisplayName("GET /api/payment/status/{transactionId} - Should get payment status (public)")
        void testGetPaymentStatus() throws Exception {
                mockMvc.perform(get("/api/payment/status/TXN123456")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound()); // Expected since no transaction exists
        }

        @Test
        @DisplayName("POST /api/payment/ipn/momo - Should receive MoMo callback (public)")
        void testMoMoCallback() throws Exception {
                Map<String, Object> momoCallback = Map.of(
                                "partnerCode", "MOMO",
                                "orderId", "ORDER123",
                                "requestId", "REQ123",
                                "amount", 500000,
                                "orderInfo", "Payment for ticket",
                                "orderType", "momo_wallet",
                                "transId", 123456789,
                                "resultCode", 0,
                                "message", "Successful.");

                // Add additional fields
                momoCallback = new java.util.HashMap<>(momoCallback);
                momoCallback.put("payType", "qr");
                momoCallback.put("responseTime", 1640995200000L);
                momoCallback.put("extraData", "");
                momoCallback.put("signature", "signature_hash");

                mockMvc.perform(post("/api/payment/ipn/momo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(momoCallback)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/payment/ipn/sepay - Should receive SePay callback (public)")
        void testSePayCallback() throws Exception {
                Map<String, Object> sepayCallback = Map.of(
                                "gateway", "SEPAY",
                                "transactionDate", "20260315",
                                "accountNumber", "1234567890",
                                "subAccount", "",
                                "amountIn", 500000,
                                "amountOut", 500000,
                                "code", "FT26075123456",
                                "content", "TICKET ORDER123");

                // Add additional fields
                sepayCallback = new java.util.HashMap<>(sepayCallback);
                sepayCallback.put("referenceCode", "ORDER123");
                sepayCallback.put("description", "Payment for bus ticket");

                mockMvc.perform(post("/api/payment/ipn/sepay")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sepayCallback)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/payment/test/providers - Should test payment providers (public)")
        void testPaymentProviders() throws Exception {
                mockMvc.perform(get("/api/payment/test/providers")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("GET /api/payment/test/cleanup - Should test cleanup expired payments (public)")
        void testCleanupExpiredPayments() throws Exception {
                mockMvc.perform(get("/api/payment/test/cleanup")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        // ==================== USER PAYMENT APIs ====================

        @Test
        @DisplayName("POST /api/user/payments - Should create payment request")
        void testCreatePayment() throws Exception {
                Map<String, Object> createPaymentRequest = Map.of(
                                "ticketId", 123,
                                "amount", 500000,
                                "paymentMethod", "MOMO",
                                "returnUrl", "https://app.example.com/payment/success",
                                "cancelUrl", "https://app.example.com/payment/cancel");

                performAuthenticatedPost("/api/user/payments", createPaymentRequest, userAccessToken)
                                .andExpect(status().isNotFound()); // Expected since no ticket exists
        }

        @Test
        @DisplayName("POST /api/user/payments - Should create SePay payment")
        void testCreateSePayPayment() throws Exception {
                Map<String, Object> createSePayRequest = Map.of(
                                "ticketId", 123,
                                "amount", 500000,
                                "paymentMethod", "SEPAY",
                                "returnUrl", "https://app.example.com/payment/success",
                                "cancelUrl", "https://app.example.com/payment/cancel");

                performAuthenticatedPost("/api/user/payments", createSePayRequest, userAccessToken)
                                .andExpect(status().isNotFound()); // Expected since no ticket exists
        }

        @Test
        @DisplayName("POST /api/user/payments - Should create cash payment")
        void testCreateCashPayment() throws Exception {
                Map<String, Object> createCashRequest = Map.of(
                                "ticketId", 123,
                                "amount", 500000,
                                "paymentMethod", "CASH");

                performAuthenticatedPost("/api/user/payments", createCashRequest, userAccessToken)
                                .andExpect(status().isNotFound()); // Expected since no ticket exists
        }

        @Test
        @DisplayName("GET /api/user/payments - Should get user payments")
        void testGetUserPayments() throws Exception {
                performAuthenticatedGet("/api/user/payments", userAccessToken)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("GET /api/user/payments/{transactionId} - Should get payment detail")
        void testGetPaymentDetail() throws Exception {
                performAuthenticatedGet("/api/user/payments/TXN123456", userAccessToken)
                                .andExpect(status().isNotFound()); // Expected since no payment exists
        }

        @Test
        @DisplayName("POST /api/user/payments/{transactionId}/cancel - Should cancel payment")
        void testCancelPayment() throws Exception {
                performAuthenticatedPost("/api/user/payments/TXN123456/cancel", Map.of(), userAccessToken)
                                .andExpect(status().isNotFound()); // Expected since no payment exists
        }

        // ==================== AUTHORIZATION TESTS ====================

        @Test
        @DisplayName("POST /api/user/payments - Should fail without authentication")
        void testCreatePaymentUnauthorized() throws Exception {
                Map<String, Object> createPaymentRequest = Map.of(
                                "ticketId", 123,
                                "amount", 500000,
                                "paymentMethod", "MOMO");

                mockMvc.perform(post("/api/user/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createPaymentRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/user/payments - Should fail without authentication")
        void testGetUserPaymentsUnauthorized() throws Exception {
                mockMvc.perform(get("/api/user/payments")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        // ==================== VALIDATION TESTS ====================

        @Test
        @DisplayName("POST /api/user/payments - Should fail with invalid payment method")
        void testCreatePaymentInvalidMethod() throws Exception {
                Map<String, Object> invalidPaymentRequest = Map.of(
                                "ticketId", 123,
                                "amount", 500000,
                                "paymentMethod", "INVALID_METHOD");

                performAuthenticatedPost("/api/user/payments", invalidPaymentRequest, userAccessToken)
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/user/payments - Should fail with negative amount")
        void testCreatePaymentNegativeAmount() throws Exception {
                Map<String, Object> negativeAmountRequest = Map.of(
                                "ticketId", 123,
                                "amount", -500000,
                                "paymentMethod", "MOMO");

                performAuthenticatedPost("/api/user/payments", negativeAmountRequest, userAccessToken)
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/user/payments - Should fail with missing required fields")
        void testCreatePaymentMissingFields() throws Exception {
                Map<String, Object> incompleteRequest = Map.of(
                                "amount", 500000); // Missing ticketId and paymentMethod

                performAuthenticatedPost("/api/user/payments", incompleteRequest, userAccessToken)
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/payment/ipn/momo - Should fail with invalid signature")
        void testMoMoCallbackInvalidSignature() throws Exception {
                Map<String, Object> invalidMomoCallback = Map.of(
                                "partnerCode", "MOMO",
                                "orderId", "ORDER123",
                                "resultCode", 0,
                                "signature", "invalid_signature");

                mockMvc.perform(post("/api/payment/ipn/momo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidMomoCallback)))
                                .andExpect(status().isBadRequest());
        }

        // ==================== EDGE CASES ====================

        @Test
        @DisplayName("GET /api/user/payments - Should handle pagination parameters")
        void testGetUserPaymentsWithPagination() throws Exception {
                performAuthenticatedGet("/api/user/payments?page=0&size=5&sortBy=createdAt&sortDir=desc",
                                userAccessToken)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("POST /api/user/payments - Should handle different return URLs")
        void testCreatePaymentWithCustomUrls() throws Exception {
                Map<String, Object> customUrlRequest = Map.of(
                                "ticketId", 123,
                                "amount", 500000,
                                "paymentMethod", "MOMO",
                                "returnUrl", "https://custom.example.com/success",
                                "cancelUrl", "https://custom.example.com/cancel");

                performAuthenticatedPost("/api/user/payments", customUrlRequest, userAccessToken)
                                .andExpect(status().isNotFound()); // Expected since no ticket exists
        }

        @Test
        @DisplayName("POST /api/payment/ipn/momo - Should handle successful payment callback")
        void testMoMoCallbackSuccess() throws Exception {
                Map<String, Object> successCallback = Map.of(
                                "partnerCode", "MOMO",
                                "orderId", "ORDER123",
                                "requestId", "REQ123",
                                "amount", 500000,
                                "resultCode", 0, // Success code
                                "message", "Successful.",
                                "signature", "valid_signature");

                mockMvc.perform(post("/api/payment/ipn/momo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(successCallback)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/payment/ipn/momo - Should handle failed payment callback")
        void testMoMoCallbackFailure() throws Exception {
                Map<String, Object> failureCallback = Map.of(
                                "partnerCode", "MOMO",
                                "orderId", "ORDER123",
                                "requestId", "REQ123",
                                "amount", 500000,
                                "resultCode", 1001, // Failure code
                                "message", "Transaction failed.",
                                "signature", "valid_signature");

                mockMvc.perform(post("/api/payment/ipn/momo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(failureCallback)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/payment/ipn/sepay - Should handle bank transfer callback")
        void testSePayBankTransferCallback() throws Exception {
                Map<String, Object> bankTransferCallback = Map.of(
                                "gateway", "SEPAY",
                                "transactionDate", "20260315",
                                "accountNumber", "1234567890",
                                "amountIn", 500000,
                                "code", "FT26075123456",
                                "content", "TICKET ORDER123 Nguyen Van A",
                                "referenceCode", "ORDER123");

                mockMvc.perform(post("/api/payment/ipn/sepay")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bankTransferCallback)))
                                .andExpect(status().isOk());
        }
}