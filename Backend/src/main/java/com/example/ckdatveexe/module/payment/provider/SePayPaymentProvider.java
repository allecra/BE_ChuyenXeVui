package com.example.ckdatveexe.module.payment.provider;

import com.example.ckdatveexe.module.payment.dto.PaymentCallbackRequest;
import com.example.ckdatveexe.module.payment.dto.PaymentCreateRequest;
import com.example.ckdatveexe.module.payment.dto.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SePayPaymentProvider implements PaymentProviderInterface {

    @Value("${payment.sepay.api-url:}")
    private String apiUrl;

    @Value("${payment.sepay.api-key:}")
    private String apiKey;

    @Value("${payment.sepay.account-number:}")
    private String accountNumber;

    @Value("${payment.sepay.account-name:}")
    private String accountName;

    @Value("${payment.sepay.bank-code:}")
    private String bankCode;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String getProviderName() {
        return "SEPAY";
    }

    @Override
    public PaymentResponse createPayment(PaymentCreateRequest request, String transactionId) {
        try {
            log.info("🏦 [SEPAY] Creating payment for transaction: {}", transactionId);

            // Generate QR code content for bank transfer
            String qrContent = generateBankTransferQR(request, transactionId);

            // Call SePay API to generate QR code
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("accountNumber", accountNumber);
            requestData.put("accountName", accountName);
            requestData.put("bankCode", bankCode);
            requestData.put("amount", request.getAmount().longValue());
            requestData.put("description", generateTransferDescription(request, transactionId));
            requestData.put("template", "compact");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

            log.debug("🏦 [SEPAY] Request data: {}", objectMapper.writeValueAsString(requestData));

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl + "/qr/create", entity, Map.class);

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setTransactionId(transactionId);
            paymentResponse.setProvider(getProviderName());
            paymentResponse.setAmount(request.getAmount());
            paymentResponse.setCurrency("VND");
            paymentResponse.setDescription(request.getDescription());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                log.debug("🏦 [SEPAY] Response: {}", objectMapper.writeValueAsString(responseBody));

                Boolean success = (Boolean) responseBody.get("success");
                if (success != null && success) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                    paymentResponse.setQrCodeUrl((String) data.get("qrDataURL"));
                    paymentResponse.setPaymentUrl((String) data.get("qrDataURL")); // Same as QR for bank transfer
                    paymentResponse.setProviderTransactionId(transactionId); // Use our transaction ID
                    paymentResponse.setExpiredAt(LocalDateTime.now().plusHours(24)); // Bank transfer expires in 24
                                                                                     // hours

                    log.info("✅ [SEPAY] Payment created successfully for transaction: {}", transactionId);
                } else {
                    String message = (String) responseBody.get("message");
                    log.error("❌ [SEPAY] Payment creation failed: {}", message);
                    throw new RuntimeException("SePay payment creation failed: " + message);
                }
            } else {
                log.error("❌ [SEPAY] API call failed with status: {}", response.getStatusCode());
                throw new RuntimeException("SePay API call failed");
            }

            return paymentResponse;

        } catch (Exception e) {
            log.error("💥 [SEPAY] Error creating payment for transaction: {}", transactionId, e);
            throw new RuntimeException("Failed to create SePay payment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyCallback(PaymentCallbackRequest callback) {
        try {
            // SePay webhook verification
            // For bank transfer, we typically verify based on transaction content and
            // amount
            return callback.getTransactionId() != null &&
                    callback.getAmount() != null &&
                    callback.getAmount() > 0;
        } catch (Exception e) {
            log.error("💥 [SEPAY] Error verifying callback", e);
            return false;
        }
    }

    @Override
    public PaymentCallbackRequest processCallback(Object rawCallback) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> callbackData = (Map<String, Object>) rawCallback;

            PaymentCallbackRequest callback = new PaymentCallbackRequest();

            // Extract transaction ID from transfer description
            String description = (String) callbackData.get("description");
            String transactionId = extractTransactionIdFromDescription(description);

            callback.setTransactionId(transactionId);
            callback.setProviderTransactionId((String) callbackData.get("transactionId"));
            callback.setAmount(Double.valueOf(callbackData.get("amount").toString()));
            callback.setMessage((String) callbackData.get("description"));
            callback.setRawData(callbackData);

            // SePay typically sends successful transactions only
            callback.setStatus("COMPLETED");

            return callback;
        } catch (Exception e) {
            log.error("💥 [SEPAY] Error processing callback", e);
            throw new RuntimeException("Failed to process SePay callback", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return apiUrl != null && !apiUrl.isEmpty() &&
                apiKey != null && !apiKey.isEmpty() &&
                accountNumber != null && !accountNumber.isEmpty() &&
                bankCode != null && !bankCode.isEmpty();
    }

    private String generateBankTransferQR(PaymentCreateRequest request, String transactionId) {
        // Generate VietQR format
        return String.format("2|99|%s|%s|%s|%s|0|0|%d|%s",
                bankCode,
                accountNumber,
                generateTransferDescription(request, transactionId),
                accountName,
                request.getAmount().longValue(),
                transactionId);
    }

    private String generateTransferDescription(PaymentCreateRequest request, String transactionId) {
        String baseDescription = request.getDescription() != null ? request.getDescription() : "Thanh toan ve xe";
        return String.format("%s - Ma GD: %s", baseDescription, transactionId);
    }

    private String extractTransactionIdFromDescription(String description) {
        if (description == null)
            return null;

        // Extract transaction ID from description format: "Description - Ma GD:
        // TRANSACTION_ID"
        String[] parts = description.split(" - Ma GD: ");
        if (parts.length == 2) {
            return parts[1].trim();
        }

        return null;
    }
}