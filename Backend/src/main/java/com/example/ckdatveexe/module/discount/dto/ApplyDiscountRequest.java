package com.example.ckdatveexe.module.discount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyDiscountRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    private String discountCode;

    @NotNull(message = "Số tiền đơn hàng không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền đơn hàng phải lớn hơn 0")
    private BigDecimal orderAmount;

    private Integer routeId;

    private Integer companyId;
}