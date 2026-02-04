package com.example.ckdatveexe.module.seat.dto;

import com.example.ckdatveexe.shared.entity.SeatType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatUpdateRequest {

    @Size(max = 20, message = "Số ghế không được vượt quá 20 ký tự")
    private String seatNumber;

    private SeatType seatType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá ghế phải lớn hơn 0")
    private Double priceForSeatType;

    private Integer rowNumber;

    private Integer columnNumber;
}