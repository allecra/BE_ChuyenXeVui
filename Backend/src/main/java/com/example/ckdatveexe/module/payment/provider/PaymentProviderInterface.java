package com.example.ckdatveexe.module.payment.provider;

import com.example.ckdatveexe.module.payment.dto.PaymentCallbackRequest;
import com.example.ckdatveexe.module.payment.dto.PaymentCreateRequest;
import com.example.ckdatveexe.module.payment.dto.PaymentResponse;

public interface PaymentProviderInterface {

    /**
     * Get provider name
     */
    String getProviderName();

    /**
     * Create payment request and return QR code
     */
    PaymentResponse createPayment(PaymentCreateRequest request, String transactionId);

    /**
     * Verify callback signature
     */
    boolean verifyCallback(PaymentCallbackRequest callback);

    /**
     * Process callback and return payment status
     */
    PaymentCallbackRequest processCallback(Object rawCallback);

    /**
     * Check if provider is enabled
     */
    boolean isEnabled();
}