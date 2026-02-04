package com.example.ckdatveexe.module.schedule.dto;

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
@Schema(description = "Request để tạo lịch trình mới")
public class ScheduleCreateRequest {

    @NotNull(message = "ID tuyến không được để trống")
    @Schema(description = "ID tuyến đường", example = "1")
    private Integer routeId;

    @NotNull(message = "ID xe không được để trống")
    @Schema(description = "ID xe", example = "1")
    private Integer busId;

    @NotNull(message = "Thời gian xuất phát không được để trống")
    @Future(message = "Thời gian xuất phát phải trong tương lai")
    @Schema(description = "Thời gian xuất phát", example = "2026-02-10T08:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureTime;

    @NotNull(message = "Thời gian đến không được để trống")
    @Schema(description = "Thời gian đến", example = "2026-02-10T20:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime arrivalTime;

    @NotNull(message = "ID bến đi không được để trống")
    @Schema(description = "ID bến xuất phát", example = "1")
    private Integer startStationId;

    @NotNull(message = "ID bến đến không được để trống")
    @Schema(description = "ID bến đến", example = "2")
    private Integer endStationId;

    @NotNull(message = "Giá vé không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá vé phải lớn hơn 0")
    @Schema(description = "Giá vé (VND)", example = "500000")
    private Double price;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    @Schema(description = "Ghi chú về lịch trình", example = "Lịch trình đặc biệt dịp lễ")
    private String notes;

    // Custom validation method
    @AssertTrue(message = "Thời gian đến phải sau thời gian xuất phát")
    public boolean isArrivalTimeAfterDepartureTime() {
        if (departureTime == null || arrivalTime == null) {
            return true; // Let @NotNull handle null validation
        }
        return arrivalTime.isAfter(departureTime);
    }

    @AssertTrue(message = "Bến đi và bến đến không được giống nhau")
    public boolean isDifferentStations() {
        if (startStationId == null || endStationId == null) {
            return true; // Let @NotNull handle null validation
        }
        return !startStationId.equals(endStationId);
    }
}