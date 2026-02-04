package com.example.ckdatveexe.module.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin giá vé theo đoạn")
public class SegmentPriceResponse {

    @Schema(description = "ID tuyến đường", example = "1")
    private Integer routeId;

    @Schema(description = "Tên tuyến đường", example = "Hà Nội - Hồ Chí Minh")
    private String routeName;

    @Schema(description = "ID bến xuất phát", example = "1")
    private Integer departureStationId;

    @Schema(description = "Tên bến xuất phát", example = "Bến xe Miền Đông")
    private String departureStationName;

    @Schema(description = "ID bến đến", example = "3")
    private Integer arrivalStationId;

    @Schema(description = "Tên bến đến", example = "Bến xe Miền Tây")
    private String arrivalStationName;

    @Schema(description = "Thứ tự bến xuất phát", example = "0")
    private Integer departureOrderIndex;

    @Schema(description = "Thứ tự bến đến", example = "2")
    private Integer arrivalOrderIndex;

    @Schema(description = "Tổng khoảng cách (km)", example = "150")
    private Integer totalDistance;

    @Schema(description = "Tổng giá vé (VND)", example = "300000")
    private Double totalPrice;

    @Schema(description = "Số bến trung gian", example = "1")
    private Integer intermediateStationCount;

    @Schema(description = "Danh sách các bến trên đoạn đường")
    private List<RouteStationResponse> stations;

    @Schema(description = "Chi tiết các đoạn giá")
    private List<SegmentDetail> segments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Chi tiết một đoạn trong tuyến")
    public static class SegmentDetail {
        @Schema(description = "Bến đầu đoạn", example = "Bến xe Miền Đông")
        private String fromStation;

        @Schema(description = "Bến cuối đoạn", example = "Bến xe Vinh")
        private String toStation;

        @Schema(description = "Khoảng cách đoạn (km)", example = "50")
        private Integer distance;

        @Schema(description = "Giá vé đoạn (VND)", example = "100000")
        private Double price;
    }
}