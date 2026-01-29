package com.example.ckdatveexe.module.seat.dto;

import com.example.ckdatveexe.shared.entity.SeatType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatCreateRequest {

    @NotBlank(message = "Số ghế không được để trống")
    @Size(max = 20, message = "Số ghế không được vượt quá 20 ký tự")
    private String seatNumber;

    @NotNull(message = "Loại ghế không được để trống")
    private SeatType seatType;

    @NotNull(message = "Giá ghế không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá ghế phải lớn hơn 0")
    private Double priceForSeatType;

    @NotNull(message = "Số hàng không được để trống")
    private Integer rowNumber;

    @NotNull(message = "Số cột không được để trống")
    private Integer columnNumber;

    @NotNull(message = "ID xe không được để trống")
    private Integer busId;
}