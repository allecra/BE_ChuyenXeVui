package com.example.ckdatveexe.module.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để thêm/cập nhật bến trong tuyến đường")
public class RouteStationRequest {

    @NotNull(message = "ID bến không được để trống")
    @Schema(description = "ID của bến xe", example = "1")
    private Integer stationId;

    @NotNull(message = "Thứ tự bến không được để trống")
    @Min(value = 0, message = "Thứ tự bến phải >= 0")
    @Schema(description = "Thứ tự bến trong tuyến (0 = bến đầu)", example = "1")
    private Integer orderIndex;

    @NotNull(message = "Khoảng cách từ bến trước không được để trống")
    @Min(value = 0, message = "Khoảng cách phải >= 0")
    @Schema(description = "Khoảng cách từ bến trước (km)", example = "50")
    private Integer distanceFromPrevious;

    @NotNull(message = "Giá từ bến trước không được để trống")
    @DecimalMin(value = "0.0", message = "Giá phải >= 0")
    @Schema(description = "Giá vé từ bến trước (VND)", example = "100000")
    private Double priceFromPrevious;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    @Schema(description = "Ghi chú về bến trong tuyến", example = "Bến trung gian, dừng 10 phút")
    private String notes;
}