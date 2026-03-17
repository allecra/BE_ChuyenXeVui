package com.example.ckdatveexe.module.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackRequest {
    private String transactionId;
    private String providerTransactionId;
    private String status;
    private Double amount;
    private String message;
    private String signature;
    private Map<String, Object> rawData;
}