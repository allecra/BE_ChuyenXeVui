package com.example.ckdatveexe.module.station.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusStationCreateRequest {

    @NotNull(message = "Bus ID không được để trống")
    private Integer busId;

    @NotNull(message = "Station ID không được để trống")
    private Integer stationId;

    private String notes;

    private Boolean isActive = true;
}