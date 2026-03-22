package com.example.ckdatveexe.module.ticket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TicketModificationResponse {
    private Integer ticketId;
    private String ticketCode;
    private BigDecimal originalAmount;
    private BigDecimal newAmount;
    private BigDecimal additionalFee;
    private BigDecimal refundAmount;
    private String modificationReason;
    private LocalDateTime modificationTime;
    private String oldScheduleInfo;
    private String newScheduleInfo;
    private String oldSeatNumbers;
    private String newSeatNumbers;
    private String message;
}