package com.example.ckdatveexe.module.schedule.dto;

import com.example.ckdatveexe.shared.entity.BusType;
import com.example.ckdatveexe.shared.entity.ScheduleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để tìm kiếm lịch trình với nhiều bộ lọc")
public class ScheduleSearchRequest {

    // Common filters for both USER and COMPANY
    @Schema(description = "ID bến xuất phát", example = "1")
    private Integer startStationId;

    @Schema(description = "ID bến đến", example = "2")
    private Integer endStationId;

    @Schema(description = "Ngày xuất phát", example = "2026-02-10T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureDate;

    @Schema(description = "Giờ xuất phát từ", example = "2026-02-10T06:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timeFrom;

    @Schema(description = "Giờ xuất phát đến", example = "2026-02-10T18:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timeTo;

    @Schema(description = "Giá tối thiểu (VND)", example = "300000")
    private Double minPrice;

    @Schema(description = "Giá tối đa (VND)", example = "800000")
    private Double maxPrice;

    @Schema(description = "ID nhà xe", example = "1")
    private Integer busCompanyId;

    @Schema(description = "Loại xe", example = "GIUONG_NAM")
    private BusType busType;

    @Schema(description = "Số chỗ ngồi tối thiểu", example = "10")
    private Integer minSeats;

    // Company-specific filters
    @Schema(description = "ID tuyến (chỉ dành cho COMPANY)", example = "1")
    private Integer routeId;

    @Schema(description = "ID xe (chỉ dành cho COMPANY)", example = "1")
    private Integer busId;

    @Schema(description = "Thời gian xuất phát từ (chỉ dành cho COMPANY)", example = "2026-02-10T06:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureTimeFrom;

    @Schema(description = "Thời gian xuất phát đến (chỉ dành cho COMPANY)", example = "2026-02-10T22:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureTimeTo;

    @Schema(description = "Thời gian đến từ (chỉ dành cho COMPANY)", example = "2026-02-10T18:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime arrivalTimeFrom;

    @Schema(description = "Thời gian đến đến (chỉ dành cho COMPANY)", example = "2026-02-11T06:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime arrivalTimeTo;

    @Schema(description = "Trạng thái lịch trình (chỉ dành cho COMPANY)", example = "ACTIVE")
    private ScheduleStatus status;

    // Pagination and sorting
    @Schema(description = "Số trang (bắt đầu từ 0)", example = "0")
    private int page = 0;

    @Schema(description = "Số lượng bản ghi mỗi trang", example = "10")
    private int size = 10;

    @Schema(description = "Trường sắp xếp", example = "departureTime", allowableValues = { "departureTime",
            "arrivalTime", "price", "createdAt" })
    private String sortBy = "departureTime";

    @Schema(description = "Hướng sắp xếp", example = "asc", allowableValues = { "asc", "desc" })
    private String sortDirection = "asc";
}