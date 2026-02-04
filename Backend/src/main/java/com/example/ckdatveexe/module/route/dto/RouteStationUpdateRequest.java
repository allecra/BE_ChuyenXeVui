package com.example.ckdatveexe.module.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để cập nhật thông tin bến trong tuyến")
public class RouteStationUpdateRequest {

    @Min(value = 0, message = "Thứ tự bến phải >= 0")
    @Schema(description = "Thứ tự bến mới trong tuyến", example = "2")
    private Integer orderIndex;

    @Min(value = 0, message = "Khoảng cách phải >= 0")
    @Schema(description = "Khoảng cách từ bến trước (km)", example = "75")
    private Integer distanceFromPrevious;

    @DecimalMin(value = "0.0", message = "Giá phải >= 0")
    @Schema(description = "Giá vé từ bến trước (VND)", example = "120000")
    private Double priceFromPrevious;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    @Schema(description = "Ghi chú về bến trong tuyến", example = "Bến trung gian, dừng 15 phút")
    private String notes;
}