package com.example.ckdatveexe.module.bus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatLayoutRequest {

    @NotNull(message = "Danh sách ghế không được để trống")
    private List<SeatLayoutItem> seats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatLayoutItem {
        private Integer seatId;
        private String seatNumber;
        private Integer rowNumber;
        private Integer columnNumber;
        private Double priceForSeatType;
    }
}