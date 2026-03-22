package com.example.ckdatveexe.module.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để tạo thanh toán")
public class PaymentCreateRequest {

    @NotNull(message = "ID vé không được để trống")
    @Schema(description = "ID vé cần thanh toán", example = "1")
    private Integer ticketId;

    @NotBlank(message = "Nhà cung cấp thanh toán không được để trống")
    @Schema(description = "Nhà cung cấp thanh toán", example = "MOMO", allowableValues = { "MOMO", "SEPAY" })
    private String provider;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1000", message = "Số tiền phải ít nhất 1,000 VND")
    @Schema(description = "Số tiền thanh toán (VND)", example = "500000")
    private Double amount;

    @Schema(description = "Mô tả thanh toán", example = "Thanh toán vé xe khách")
    private String description;

    @Schema(description = "URL redirect sau khi thanh toán", example = "https://app.example.com/payment/result")
    private String returnUrl;
}