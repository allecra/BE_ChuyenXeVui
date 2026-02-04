package com.example.ckdatveexe.module.station.dto;

import com.example.ckdatveexe.module.bus.dto.BusResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusStationResponse {

    private Integer id;
    private BusResponse bus;
    private Integer stationId;
    private String stationName;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor for basic response
    public BusStationResponse(Integer id, Integer stationId, String stationName, Boolean isActive, String notes,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.stationId = stationId;
        this.stationName = stationName;
        this.isActive = isActive;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}