package com.example.ckdatveexe.module.route.dto;

import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.shared.entity.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin tuyến đường")
public class RouteResponse {

    @Schema(description = "ID tuyến đường", example = "1")
    private Integer id;

    @Schema(description = "Tên tuyến đường", example = "Hà Nội - Hồ Chí Minh")
    private String routeName;

    @Schema(description = "Điểm xuất phát", example = "Hà Nội")
    private String startLocation;

    @Schema(description = "Điểm đến", example = "Hồ Chí Minh")
    private String endLocation;

    @Schema(description = "Giá tuyến (VND)", example = "500000")
    private Double price;

    @Schema(description = "Thời gian di chuyển (phút)", example = "1200")
    private Integer duration;

    @Schema(description = "Khoảng cách (km)", example = "1700")
    private Integer distance;

    @Schema(description = "Mô tả tuyến đường", example = "Tuyến cao tốc, đi qua các tỉnh miền Trung")
    private String descriptions;

    @Schema(description = "Trạng thái tuyến đường", example = "ACTIVE")
    private RouteStatus status;

    @Schema(description = "ID nhà xe", example = "1")
    private Integer busCompanyId;

    @Schema(description = "Tên nhà xe", example = "Phương Trang Express")
    private String busCompanyName;

    @Schema(description = "ID bến xuất phát", example = "1")
    private Integer departureStationId;

    @Schema(description = "Tên bến xuất phát", example = "Bến xe Miền Đông")
    private String departureStationName;

    @Schema(description = "ID bến đến", example = "2")
    private Integer arrivalStationId;

    @Schema(description = "Tên bến đến", example = "Bến xe Miền Tây")
    private String arrivalStationName;

    @Schema(description = "Số lượng lịch trình hoạt động", example = "5")
    private Integer activeScheduleCount;

    @Schema(description = "Số lượng xe đang hoạt động trên tuyến", example = "3")
    private Integer activeBusCount;

    @Schema(description = "Thời gian tạo", example = "2026-02-04T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật", example = "2026-02-04T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Thời gian xóa (nếu có)", example = "2026-02-04T10:30:00")
    private LocalDateTime deletedAt;

    @Schema(description = "Danh sách xe trên tuyến (chỉ hiển thị khi xem chi tiết)")
    private List<BusResponse> buses;

    // Static factory methods for different response types
    public static RouteResponse forList(Integer id, String routeName, String startLocation, String endLocation,
            Double price, Integer duration, Integer distance, RouteStatus status,
            Integer busCompanyId, String busCompanyName,
            String departureStationName, String arrivalStationName,
            Integer activeScheduleCount, Integer activeBusCount,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        RouteResponse response = new RouteResponse();
        response.setId(id);
        response.setRouteName(routeName);
        response.setStartLocation(startLocation);
        response.setEndLocation(endLocation);
        response.setPrice(price);
        response.setDuration(duration);
        response.setDistance(distance);
        response.setStatus(status);
        response.setBusCompanyId(busCompanyId);
        response.setBusCompanyName(busCompanyName);
        response.setDepartureStationName(departureStationName);
        response.setArrivalStationName(arrivalStationName);
        response.setActiveScheduleCount(activeScheduleCount);
        response.setActiveBusCount(activeBusCount);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);
        return response;
    }
}