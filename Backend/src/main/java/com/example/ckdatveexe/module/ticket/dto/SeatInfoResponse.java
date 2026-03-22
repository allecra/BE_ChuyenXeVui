package com.example.ckdatveexe.module.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin ghế trong sơ đồ")
public class SeatInfoResponse {

    @Schema(description = "ID ghế", example = "1")
    private Integer seatId;

    @Schema(description = "Số ghế", example = "A1")
    private String seatNumber;

    @Schema(description = "Loại ghế", example = "VIP")
    private String seatType;

    @Schema(description = "Giá ghế", example = "350000")
    private Double price;

    @Schema(description = "Trạng thái ghế", example = "AVAILABLE")
    private String status;

    @Schema(description = "Vị trí ghế")
    private SeatPosition position;

    @Schema(description = "Thời gian lock hết hạn (nếu đang lock)", example = "2026-03-11T08:15:00")
    private LocalDateTime lockedUntil;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatPosition {
        @Schema(description = "Hàng", example = "1")
        private Integer row;

        @Schema(description = "Cột", example = "1")
        private Integer column;
    }
}