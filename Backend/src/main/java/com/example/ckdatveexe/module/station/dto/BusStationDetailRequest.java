package com.example.ckdatveexe.module.station.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusStationDetailRequest {

    @NotNull(message = "Station ID không được để trống")
    private Integer stationId;

    @NotEmpty(message = "Danh sách bus-station không được để trống")
    @Valid
    private List<BusStationCreateRequest> busStations;

    private boolean replaceAll = false;
}