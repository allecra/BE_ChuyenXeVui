package com.example.ckdatveexe.module.station.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedAssignBusRequest {

    @NotNull(message = "Station ID không được để trống")
    private Integer stationId;

    private boolean replaceAll = false;

    // Cách 1: Cơ bản - chỉ cần danh sách bus IDs
    private List<Integer> busIds;

    // Cách 2: Chi tiết - danh sách bus với thông tin chi tiết
    @Valid
    private List<BusStationCreateRequest> busStations;

    // Validation: Phải có ít nhất một trong hai
    public boolean isValid() {
        return (busIds != null && !busIds.isEmpty()) ||
                (busStations != null && !busStations.isEmpty());
    }

    // Helper methods
    public boolean isBasicMode() {
        return busIds != null && !busIds.isEmpty();
    }

    public boolean isDetailedMode() {
        return busStations != null && !busStations.isEmpty();
    }
}