package com.example.ckdatveexe.module.schedule.dto;

import com.example.ckdatveexe.shared.entity.BusType;
import com.example.ckdatveexe.shared.entity.ScheduleBusStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin xe trong lịch trình")
public class ScheduleBusResponse {

    @Schema(description = "ID liên kết lịch trình-xe", example = "1")
    private Integer id;

    @Schema(description = "ID lịch trình", example = "1")
    private Integer scheduleId;

    @Schema(description = "ID xe", example = "1")
    private Integer busId;

    @Schema(description = "Tên xe", example = "Xe Limousine VIP")
    private String busName;

    @Schema(description = "Biển số xe", example = "30A-12345")
    private String busLicensePlate;

    @Schema(description = "Loại xe", example = "GIUONG_NAM")
    private BusType busType;

    @Schema(description = "Số chỗ ngồi", example = "22")
    private Integer busCapacity;

    @Schema(description = "Trạng thái xe trong lịch trình", example = "ACTIVE")
    private ScheduleBusStatus status;

    @Schema(description = "Thời gian gán xe vào lịch trình", example = "2026-02-04T10:30:00")
    private LocalDateTime createdAt;

    // Static factory method
    public static ScheduleBusResponse from(Integer id, Integer scheduleId, Integer busId,
            String busName, String busLicensePlate, BusType busType,
            Integer busCapacity, ScheduleBusStatus status,
            LocalDateTime createdAt) {
        return new ScheduleBusResponse(id, scheduleId, busId, busName, busLicensePlate,
                busType, busCapacity, status, createdAt);
    }
}