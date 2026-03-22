package com.example.ckdatveexe.module.discount.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountUsageResponse {
    private Integer id;
    private String userEmail;
    private String discountCode;
    private String ticketCode;
    private BigDecimal discountAmount;
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private LocalDateTime usedAt;
}