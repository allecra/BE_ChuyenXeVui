package com.example.ckdatveexe.module.schedule.dto;

import com.example.ckdatveexe.shared.entity.BusType;
import com.example.ckdatveexe.shared.entity.ScheduleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin lịch trình")
public class ScheduleResponse {

    @Schema(description = "ID lịch trình", example = "1")
    private Integer id;

    @Schema(description = "ID tuyến đường", example = "1")
    private Integer routeId;

    @Schema(description = "Tên tuyến đường", example = "Hà Nội - Hồ Chí Minh")
    private String routeName;

    @Schema(description = "ID xe", example = "1")
    private Integer busId;

    @Schema(description = "Tên xe", example = "Xe Limousine VIP")
    private String busName;

    @Schema(description = "Biển số xe", example = "30A-12345")
    private String busLicensePlate;

    @Schema(description = "Loại xe", example = "GIUONG_NAM")
    private BusType busType;

    @Schema(description = "ID nhà xe", example = "1")
    private Integer busCompanyId;

    @Schema(description = "Tên nhà xe", example = "Phương Trang Express")
    private String busCompanyName;

    @Schema(description = "Thời gian xuất phát", example = "2026-02-10T08:00:00")
    private LocalDateTime departureTime;

    @Schema(description = "Thời gian đến", example = "2026-02-10T20:00:00")
    private LocalDateTime arrivalTime;

    @Schema(description = "Thời gian di chuyển (phút)", example = "720")
    private Long travelDurationMinutes;

    @Schema(description = "ID bến xuất phát", example = "1")
    private Integer startStationId;

    @Schema(description = "Tên bến xuất phát", example = "Bến xe Miền Đông")
    private String startStationName;

    @Schema(description = "Địa điểm bến xuất phát", example = "Hà Nội")
    private String startStationLocation;

    @Schema(description = "ID bến đến", example = "2")
    private Integer endStationId;

    @Schema(description = "Tên bến đến", example = "Bến xe Miền Tây")
    private String endStationName;

    @Schema(description = "Địa điểm bến đến", example = "Hồ Chí Minh")
    private String endStationLocation;

    @Schema(description = "Giá vé (VND)", example = "500000")
    private Double price;

    @Schema(description = "Số chỗ còn trống", example = "15")
    private Integer availableSeat;

    @Schema(description = "Tổng số chỗ", example = "22")
    private Integer totalSeats;

    @Schema(description = "Tỷ lệ lấp đầy (%)", example = "31.8")
    private Double occupancyRate;

    @Schema(description = "Trạng thái lịch trình", example = "ACTIVE")
    private ScheduleStatus status;

    @Schema(description = "Ghi chú", example = "Lịch trình đặc biệt dịp lễ")
    private String notes;

    @Schema(description = "Danh sách xe trong lịch trình")
    private List<ScheduleBusResponse> buses;

    @Schema(description = "Số lượng xe đang hoạt động", example = "2")
    private Integer activeBusCount;

    @Schema(description = "Thời gian tạo", example = "2026-02-04T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật", example = "2026-02-04T10:30:00")
    private LocalDateTime updatedAt;

    // Helper methods
    public void calculateTravelDuration() {
        if (departureTime != null && arrivalTime != null) {
            this.travelDurationMinutes = java.time.Duration.between(departureTime, arrivalTime).toMinutes();
        }
    }

    public void calculateOccupancyRate() {
        if (totalSeats != null && availableSeat != null && totalSeats > 0) {
            int occupiedSeats = totalSeats - availableSeat;
            this.occupancyRate = (double) occupiedSeats / totalSeats * 100;
            this.occupancyRate = Math.round(this.occupancyRate * 10.0) / 10.0; // Round to 1 decimal place
        }
    }

    // Static factory method for list view (minimal data)
    public static ScheduleResponse forList(Integer id, Integer routeId, String routeName,
            Integer busId, String busName, String busLicensePlate, BusType busType,
            Integer busCompanyId, String busCompanyName,
            LocalDateTime departureTime, LocalDateTime arrivalTime,
            Integer startStationId, String startStationName, String startStationLocation,
            Integer endStationId, String endStationName, String endStationLocation,
            Double price, Integer availableSeat, Integer totalSeats,
            ScheduleStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        ScheduleResponse response = new ScheduleResponse();
        response.setId(id);
        response.setRouteId(routeId);
        response.setRouteName(routeName);
        response.setBusId(busId);
        response.setBusName(busName);
        response.setBusLicensePlate(busLicensePlate);
        response.setBusType(busType);
        response.setBusCompanyId(busCompanyId);
        response.setBusCompanyName(busCompanyName);
        response.setDepartureTime(departureTime);
        response.setArrivalTime(arrivalTime);
        response.setStartStationId(startStationId);
        response.setStartStationName(startStationName);
        response.setStartStationLocation(startStationLocation);
        response.setEndStationId(endStationId);
        response.setEndStationName(endStationName);
        response.setEndStationLocation(endStationLocation);
        response.setPrice(price);
        response.setAvailableSeat(availableSeat);
        response.setTotalSeats(totalSeats);
        response.setStatus(status);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);

        // Calculate derived fields
        response.calculateTravelDuration();
        response.calculateOccupancyRate();

        return response;
    }
}