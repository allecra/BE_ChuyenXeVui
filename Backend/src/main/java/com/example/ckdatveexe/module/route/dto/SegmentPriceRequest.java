package com.example.ckdatveexe.module.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để tính giá vé theo đoạn")
public class SegmentPriceRequest {

    @NotNull(message = "ID tuyến không được để trống")
    @Schema(description = "ID tuyến đường", example = "1")
    private Integer routeId;

    @NotNull(message = "ID bến đi không được để trống")
    @Schema(description = "ID bến xuất phát", example = "1")
    private Integer departureStationId;

    @NotNull(message = "ID bến đến không được để trống")
    @Schema(description = "ID bến đến", example = "3")
    private Integer arrivalStationId;
}