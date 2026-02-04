package com.example.ckdatveexe.module.route.dto;

import com.example.ckdatveexe.shared.entity.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để tìm kiếm tuyến đường với nhiều bộ lọc")
public class RouteSearchRequest {

    @Schema(description = "Điểm xuất phát", example = "Hà Nội")
    private String startLocation;

    @Schema(description = "Điểm đến", example = "Hồ Chí Minh")
    private String endLocation;

    @Schema(description = "Giá tối thiểu (VND)", example = "300000")
    private Double minPrice;

    @Schema(description = "Giá tối đa (VND)", example = "800000")
    private Double maxPrice;

    @Schema(description = "ID nhà xe", example = "1")
    private Integer busCompanyId;

    @Schema(description = "Tên nhà xe", example = "Phương Trang")
    private String busCompanyName;

    @Schema(description = "Ngày xuất phát", example = "2026-02-10T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureDate;

    @Schema(description = "Giờ xuất phát từ", example = "2026-02-10T06:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureTimeFrom;

    @Schema(description = "Giờ xuất phát đến", example = "2026-02-10T18:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureTimeTo;

    @Schema(description = "Ngày đến", example = "2026-02-10T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime arrivalDate;

    @Schema(description = "Giờ đến từ", example = "2026-02-10T18:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime arrivalTimeFrom;

    @Schema(description = "Giờ đến đến", example = "2026-02-11T06:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime arrivalTimeTo;

    @Schema(description = "Trạng thái tuyến (chỉ dành cho COMPANY)", example = "ACTIVE")
    private RouteStatus status;

    @Schema(description = "Số trang (bắt đầu từ 0)", example = "0")
    private int page = 0;

    @Schema(description = "Số lượng bản ghi mỗi trang", example = "10")
    private int size = 10;

    @Schema(description = "Trường sắp xếp", example = "price", allowableValues = { "price", "duration", "createdAt",
            "routeName" })
    private String sortBy = "createdAt";

    @Schema(description = "Hướng sắp xếp", example = "asc", allowableValues = { "asc", "desc" })
    private String sortDirection = "desc";
}