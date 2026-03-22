package com.example.ckdatveexe.module.ticket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TicketCancellationResponse {
    private Integer ticketId;
    private String ticketCode;
    private BigDecimal originalAmount;
    private BigDecimal refundAmount;
    private BigDecimal cancellationFee;
    private Integer refundPercentage;
    private String cancellationReason;
    private LocalDateTime cancellationTime;
    private LocalDateTime estimatedRefundTime;
    private String refundMethod;
    private String refundStatus;
    private String message;
}