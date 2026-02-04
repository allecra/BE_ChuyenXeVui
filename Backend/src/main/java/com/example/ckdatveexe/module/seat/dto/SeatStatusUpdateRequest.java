package com.example.ckdatveexe.module.seat.dto;

import com.example.ckdatveexe.shared.entity.SeatStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusUpdateRequest {

    @NotNull(message = "Trạng thái ghế không được để trống")
    private SeatStatus status;

    private String reason; // Lý do thay đổi trạng thái (optional)
}