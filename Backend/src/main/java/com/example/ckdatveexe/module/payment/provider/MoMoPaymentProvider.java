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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MoMoPaymentProvider implements PaymentProviderInterface {

    @Value("${payment.momo.partner-code:}")
    private String partnerCode;

    @Value("${payment.momo.access-key:}")
    private String accessKey;

    @Value("${payment.momo.secret-key:}")
    private String secretKey;

    @Value("${payment.momo.api-endpoint:}")
    private String apiEndpoint;

    @Value("${payment.momo.redirect-url:}")
    private String redirectUrl;

    @Value("${payment.momo.ipn-url:}")
    private String ipnUrl;

    @Value("${payment.momo.request-type:captureWallet}")
    private String requestType;

    @Value("${payment.momo.auto-capture:true}")
    private boolean autoCapture;

    @Value("${payment.momo.lang:vi}")
    private String lang;

    @Value("${payment.momo.debug:false}")
    private boolean debug;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String getProviderName() {
        return "MOMO";
    }

    @Override
    public PaymentResponse createPayment(PaymentCreateRequest request, String transactionId) {
        try {
            log.info("🏦 [MOMO] Creating payment for transaction: {}", transactionId);

            // Prepare request data
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("partnerCode", partnerCode);
            requestData.put("partnerName", "CK Dat Ve Xe");
            requestData.put("storeId", partnerCode);
            requestData.put("requestId", transactionId);
            requestData.put("amount", request.getAmount().longValue());
            requestData.put("orderId", transactionId);
            requestData.put("orderInfo", request.getDescription() != null ? request.getDescription() : "Thanh toán vé xe");
            requestData.put("redirectUrl", request.getReturnUrl() != null ? request.getReturnUrl() : redirectUrl);
            requestData.put("ipnUrl", ipnUrl);
            requestData.put("lang", lang);
            requestData.put("requestType", requestType);
            requestData.put("autoCapture", autoCapture);
            requestData.put("extraData", "");

            // Generate signature
            String signature = generateSignature(requestData);
            requestData.put("signature", signature);

            if (debug) {
                log.debug("🏦 [MOMO] Request data: {}", objectMapper.writeValueAsString(requestData));
            }

            // Call MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiEndpoint, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if (debug) {
                    log.debug("🏦 [MOMO] Response: {}", objectMapper.writeValueAsString(responseBody));
                }

                PaymentResponse paymentResponse = new PaymentResponse();
                paymentResponse.setTransactionId(transactionId);
                paymentResponse.setProvider(getProviderName());
                paymentResponse.setAmount(request.getAmount());
                paymentResponse.setCurrency("VND");
                paymentResponse.setDescription(request.getDescription());

                // Check response status
                Integer resultCode = (Integer) responseBody.get("resultCode");
                if (resultCode != null && resultCode == 0) {
                    paymentResponse.setQrCodeUrl((String) responseBody.get("qrCodeUrl"));
                    paymentResponse.setPaymentUrl((String) responseBody.get("payUrl"));
                    paymentResponse.setProviderTransactionId((String) responseBody.get("transId"));
                    paymentResponse.setExpiredAt(LocalDateTime.now().plusMinutes(15)); // MoMo QR expires in 15 minutes
                    
                    log.info("✅ [MOMO] Payment created successfully for transaction: {}", transactionId);
                } else {
                    String message = (String) responseBody.get("message");
                    log.error("❌ [MOMO] Payment creation failed: {} - {}", resultCode, message);
                    throw new RuntimeException("MoMo payment creation failed: " + message);
                }

                return paymentResponse;
            } else {
                log.error("❌ [MOMO] API call failed with status: {}", response.getStatusCode());
                throw new RuntimeException("MoMo API call failed");
            }

        } catch (Exception e) {
            log.error("💥 [MOMO] Error creating payment for transaction: {}", transactionId, e);
            throw new RuntimeException("Failed to create MoMo payment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyCallback(PaymentCallbackRequest callback) {
        try {
            // Verify MoMo signature
            Map<String, Object> data = new HashMap<>();
            data.put("partnerCode", partnerCode);
            data.put("orderId", callback.getTransactionId());
            data.put("requestId", callback.getTransactionId());
            data.put("amount", callback.getAmount().longValue());
            data.put("orderInfo", "");
            data.put("orderType", "");
            data.put("transId", callback.getProviderTransactionId());
            data.put("resultCode", "0".equals(callback.getStatus()) ? 0 : 1);
            data.put("message", callback.getMessage());
            data.put("payType", "");
            data.put("responseTime", "");
            data.put("extraData", "");

            String expectedSignature = generateSignature(data);
            boolean isValid = expectedSignature.equals(callback.getSignature());

            if (debug) {
                log.debug("🏦 [MOMO] Signature verification - Expected: {}, Received: {}, Valid: {}", 
                         expectedSignature, callback.getSignature(), isValid);
            }

            return isValid;
        } catch (Exception e) {
            log.error("💥 [MOMO] Error verifying callback signature", e);
            return false;
        }
    }

    @Override
    public PaymentCallbackRequest processCallback(Object rawCallback) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> callbackData = (Map<String, Object>) rawCallback;

            PaymentCallbackRequest callback = new PaymentCallbackRequest();
            callback.setTransactionId((String) callbackData.get("orderId"));
            callback.setProviderTransactionId(String.valueOf(callbackData.get("transId")));
            callback.setAmount(Double.valueOf(callbackData.get("amount").toString()));
            callback.setMessage((String) callbackData.get("message"));
            callback.setSignature((String) callbackData.get("signature"));
            callback.setRawData(callbackData);

            // Convert MoMo result code to status
            Integer resultCode = (Integer) callbackData.get("resultCode");
            if (resultCode != null && resultCode == 0) {
                callback.setStatus("COMPLETED");
            } else {
                callback.setStatus("FAILED");
            }

            return callback;
        } catch (Exception e) {
            log.error("💥 [MOMO] Error processing callback", e);
            throw new RuntimeException("Failed to process MoMo callback", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return partnerCode != null && !partnerCode.isEmpty() &&
               accessKey != null && !accessKey.isEmpty() &&
               secretKey != null && !secretKey.isEmpty() &&
               apiEndpoint != null && !apiEndpoint.isEmpty();
    }

    private String generateSignature(Map<String, Object> data) {
        try {
            // Build signature string according to MoMo specification
            StringBuilder signatureBuilder = new StringBuilder();
            signatureBuilder.append("accessKey=").append(accessKey);
            signatureBuilder.append("&amount=").append(data.get("amount"));
            signatureBuilder.append("&extraData=").append(data.getOrDefault("extraData", ""));
            signatureBuilder.append("&ipnUrl=").append(data.getOrDefault("ipnUrl", ipnUrl));
            signatureBuilder.append("&orderId=").append(data.get("orderId"));
            signatureBuilder.append("&orderInfo=").append(data.get("orderInfo"));
            signatureBuilder.append("&partnerCode=").append(partnerCode);
            signatureBuilder.append("&redirectUrl=").append(data.getOrDefault("redirectUrl", redirectUrl));
            signatureBuilder.append("&requestId=").append(data.get("requestId"));
            signatureBuilder.append("&requestType=").append(requestType);

            String signatureString = signatureBuilder.toString();

            if (debug) {
                log.debug("🏦 [MOMO] Signature string: {}", signatureString);
            }

            // Generate HMAC SHA256 signature
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(signatureString.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            log.error("💥 [MOMO] Error generating signature", e);
            throw new RuntimeException("Failed to generate MoMo signature", e);
        }
    }
}