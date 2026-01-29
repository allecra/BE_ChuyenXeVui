package com.example.ckdatveexe.module.seat.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatPriceUpdateRequest {

    @NotNull(message = "Giá ghế không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá ghế phải lớn hơn 0")
    private Double priceForSeatType;

    private String reason; // Lý do thay đổi giá (optional)
}