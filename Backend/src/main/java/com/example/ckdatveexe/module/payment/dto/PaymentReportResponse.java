package com.example.ckdatveexe.module.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PaymentReportResponse {
    private LocalDateTime reportDate;
    private Integer totalPayments;
    private Integer successfulPayments;
    private Integer failedPayments;
    private Integer pendingPayments;
    private Integer refundedPayments;
    private BigDecimal totalAmount;
    private BigDecimal successfulAmount;
    private BigDecimal refundedAmount;
    private BigDecimal netRevenue;
    private Map<String, Integer> paymentsByMethod;
    private Map<String, BigDecimal> amountByMethod;
    private Map<String, Integer> paymentsByProvider;
    private Map<String, BigDecimal> amountByProvider;
    private BigDecimal averagePaymentAmount;
    private Double successRate;
}