package com.example.ckdatveexe.module.schedule.dto;

import com.example.ckdatveexe.shared.entity.ScheduleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để cập nhật lịch trình")
public class ScheduleUpdateRequest {

    @Schema(description = "Thời gian xuất phát", example = "2026-02-10T09:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureTime;

    @Schema(description = "Thời gian đến", example = "2026-02-10T21:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime arrivalTime;

    @Schema(description = "ID bến xuất phát", example = "1")
    private Integer startStationId;

    @Schema(description = "ID bến đến", example = "2")
    private Integer endStationId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá vé phải lớn hơn 0")
    @Schema(description = "Giá vé (VND)", example = "550000")
    private Double price;

    @Schema(description = "Trạng thái lịch trình", example = "ACTIVE")
    private ScheduleStatus status;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    @Schema(description = "Ghi chú về lịch trình", example = "Đã cập nhật giá vé")
    private String notes;

    // Custom validation method
    @AssertTrue(message = "Thời gian đến phải sau thời gian xuất phát")
    public boolean isArrivalTimeAfterDepartureTime() {
        if (departureTime == null || arrivalTime == null) {
            return true; // Skip validation if either is null
        }
        return arrivalTime.isAfter(departureTime);
    }

    @AssertTrue(message = "Bến đi và bến đến không được giống nhau")
    public boolean isDifferentStations() {
        if (startStationId == null || endStationId == null) {
            return true; // Skip validation if either is null
        }
        return !startStationId.equals(endStationId);
    }
}