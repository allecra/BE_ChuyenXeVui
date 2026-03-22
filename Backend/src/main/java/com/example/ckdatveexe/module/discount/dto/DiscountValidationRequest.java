package com.example.ckdatveexe.module.discount.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiscountValidationRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    private String code;

    @NotNull(message = "Số tiền đơn hàng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền đơn hàng phải lớn hơn 0")
    private BigDecimal orderAmount;

    private Integer routeId;

    private Integer busCompanyId;
}