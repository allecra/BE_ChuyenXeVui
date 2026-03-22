package com.example.ckdatveexe.module.discount.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiscountValidationResponse {
    private Boolean isValid;
    private String message;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private BigDecimal originalAmount;
    private DiscountCodeResponse discountCode;

    public static DiscountValidationResponse invalid(String message) {
        DiscountValidationResponse response = new DiscountValidationResponse();
        response.setIsValid(false);
        response.setMessage(message);
        return response;
    }

    public static DiscountValidationResponse valid(BigDecimal originalAmount, BigDecimal discountAmount,
            BigDecimal finalAmount, DiscountCodeResponse discountCode) {
        DiscountValidationResponse response = new DiscountValidationResponse();
        response.setIsValid(true);
        response.setMessage("Mã giảm giá hợp lệ");
        response.setOriginalAmount(originalAmount);
        response.setDiscountAmount(discountAmount);
        response.setFinalAmount(finalAmount);
        response.setDiscountCode(discountCode);
        return response;
    }
}