package com.example.ckdatveexe.module.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sơ đồ ghế của lịch trình")
public class ScheduleSeatsResponse {

    @Schema(description = "ID lịch trình", example = "1")
    private Integer scheduleId;

    @Schema(description = "Thông tin xe")
    private BusInfo busInfo;

    @Schema(description = "Danh sách ghế")
    private List<SeatInfoResponse> seats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusInfo {
        @Schema(description = "Biển số xe", example = "29B-12345")
        private String busNumber;

        @Schema(description = "Loại xe", example = "LIMOUSINE")
        private String busType;

        @Schema(description = "Tổng số ghế", example = "40")
        private Integer totalSeats;

        @Schema(description = "Số ghế còn trống", example = "25")
        private Integer availableSeats;
    }
}