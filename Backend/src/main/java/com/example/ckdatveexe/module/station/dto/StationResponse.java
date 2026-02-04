package com.example.ckdatveexe.module.station.dto;

import com.example.ckdatveexe.module.bus.dto.BusResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Response thông tin bến xe")
public class StationResponse {

    @Schema(description = "ID bến xe", example = "1")
    private Integer id;

    @Schema(description = "Tên bến xe", example = "Bến xe Miền Đông")
    private String name;

    @Schema(description = "URL hình ảnh bến xe", example = "https://example.com/station.jpg")
    private String image;

    @Schema(description = "URL hình nền bến xe", example = "https://example.com/wallpaper.jpg")
    private String wallpaper;

    @Schema(description = "Mô tả chi tiết về bến xe", example = "Bến xe lớn nhất khu vực miền Đông")
    private String descriptions;

    @Schema(description = "Địa chỉ bến xe", example = "292 Đinh Bộ Lĩnh, Phường 26, Bình Thạnh, TP.HCM")
    private String location;

    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật")
    private LocalDateTime updatedAt;

    // Statistics
    @Schema(description = "Tổng số xe tại bến")
    private Integer totalBuses;

    @Schema(description = "Số xe đang hoạt động")
    private Integer activeBuses;

    // Detailed information (for detailed view)
    @Schema(description = "Danh sách xe tại bến (chỉ có khi xem chi tiết)")
    private List<BusResponse> buses;

    // Factory methods for different response types
    public static StationResponse forList(Integer id, String name, String image, String location,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            Integer totalBuses, Integer activeBuses) {
        StationResponse response = new StationResponse();
        response.setId(id);
        response.setName(name);
        response.setImage(image);
        response.setLocation(location);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);
        response.setTotalBuses(totalBuses);
        response.setActiveBuses(activeBuses);
        return response;
    }
}