package com.example.ckdatveexe.module.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularRouteResponse {
    private Integer routeId;
    private String routeName;
    private String startStationName;
    private String endStationName;
    private String startProvince;
    private String endProvince;
    private Long totalBookings;
    private Double averagePrice;
    private Integer totalSchedules;
    private String description;
}