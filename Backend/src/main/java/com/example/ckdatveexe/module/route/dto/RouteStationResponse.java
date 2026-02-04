package com.example.ckdatveexe.module.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin bến trong tuyến đường")
public class RouteStationResponse {

    @Schema(description = "ID của RouteStation", example = "1")
    private Integer id;

    @Schema(description = "ID tuyến đường", example = "1")
    private Integer routeId;

    @Schema(description = "Tên tuyến đường", example = "Hà Nội - Hồ Chí Minh")
    private String routeName;

    @Schema(description = "ID bến xe", example = "1")
    private Integer stationId;

    @Schema(description = "Tên bến xe", example = "Bến xe Miền Đông")
    private String stationName;

    @Schema(description = "Địa điểm bến xe", example = "Hà Nội")
    private String stationLocation;

    @Schema(description = "Thứ tự bến trong tuyến", example = "1")
    private Integer orderIndex;

    @Schema(description = "Khoảng cách từ bến trước (km)", example = "50")
    private Integer distanceFromPrevious;

    @Schema(description = "Giá vé từ bến trước (VND)", example = "100000")
    private Double priceFromPrevious;

    @Schema(description = "Khoảng cách tích lũy từ bến đầu (km)", example = "150")
    private Integer cumulativeDistance;

    @Schema(description = "Giá vé tích lũy từ bến đầu (VND)", example = "300000")
    private Double cumulativePrice;

    @Schema(description = "Ghi chú về bến trong tuyến", example = "Bến trung gian, dừng 10 phút")
    private String notes;

    @Schema(description = "Loại bến", example = "DEPARTURE", allowableValues = { "DEPARTURE", "INTERMEDIATE",
            "ARRIVAL" })
    private String stationType;

    @Schema(description = "Thời gian tạo", example = "2026-02-04T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật", example = "2026-02-04T10:30:00")
    private LocalDateTime updatedAt;

    // Helper method to determine station type
    public void setStationType(Integer orderIndex, Integer maxOrderIndex) {
        if (orderIndex == 0) {
            this.stationType = "DEPARTURE";
        } else if (orderIndex.equals(maxOrderIndex)) {
            this.stationType = "ARRIVAL";
        } else {
            this.stationType = "INTERMEDIATE";
        }
    }
}