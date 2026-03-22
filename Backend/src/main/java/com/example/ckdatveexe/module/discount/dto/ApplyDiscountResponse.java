package com.example.ckdatveexe.module.discount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyDiscountResponse {
    private Integer discountCodeId;
    private String discountCode;
    private String discountName;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String discountType;
    private BigDecimal discountValue;
    private boolean isValid;
    private String message;

    public static ApplyDiscountResponse success(Integer discountCodeId, String discountCode, String discountName,
            BigDecimal originalAmount, BigDecimal discountAmount,
            String discountType, BigDecimal discountValue) {
        ApplyDiscountResponse response = new ApplyDiscountResponse();
        response.setDiscountCodeId(discountCodeId);
        response.setDiscountCode(discountCode);
        response.setDiscountName(discountName);
        response.setOriginalAmount(originalAmount);
        response.setDiscountAmount(discountAmount);
        response.setFinalAmount(originalAmount.subtract(discountAmount));
        response.setDiscountType(discountType);
        response.setDiscountValue(discountValue);
        response.setValid(true);
        response.setMessage("Áp dụng mã giảm giá thành công");
        return response;
    }

    public static ApplyDiscountResponse error(String message) {
        ApplyDiscountResponse response = new ApplyDiscountResponse();
        response.setValid(false);
        response.setMessage(message);
        response.setDiscountAmount(BigDecimal.ZERO);
        return response;
    }
}