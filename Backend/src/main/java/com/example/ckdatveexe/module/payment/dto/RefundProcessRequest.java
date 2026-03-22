package com.example.ckdatveexe.module.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundProcessRequest {

    @NotNull(message = "ID thanh toán không được để trống")
    private Integer paymentId;

    @NotNull(message = "Số tiền hoàn không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền hoàn phải lớn hơn 0")
    private BigDecimal refundAmount;

    private String refundReason;

    private String bankAccountNumber;

    private String bankAccountName;

    private String bankName;
}