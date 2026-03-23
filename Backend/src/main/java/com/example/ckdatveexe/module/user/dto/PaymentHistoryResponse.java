package com.example.ckdatveexe.module.user.dto;

import com.example.ckdatveexe.shared.entity.Payment;
import com.example.ckdatveexe.shared.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private Integer paymentId;
    private String transactionId;
    private String ticketCode;
    private String routeName;
    private Double amount;
    private String currency;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String description;
    private String providerName;

    public static PaymentHistoryResponse fromEntity(Payment payment) {
        return PaymentHistoryResponse.builder()
                .paymentId(payment.getId())
                .transactionId(payment.getTransactionId())
                .ticketCode(payment.getTicket() != null ? payment.getTicket().getTicketCode() : null)
                .routeName(payment.getTicket() != null ? payment.getTicket().getSchedule().getRoute().getRouteName()
                        : null)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod().name())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .description(payment.getDescription())
                .providerName(
                        payment.getPaymentProvider() != null ? payment.getPaymentProvider().getProviderName() : null)
                .build();
    }
}