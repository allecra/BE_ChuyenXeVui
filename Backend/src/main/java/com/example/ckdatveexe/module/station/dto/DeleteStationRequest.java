package com.example.ckdatveexe.module.station.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request xóa bến xe")
public class DeleteStationRequest {

    @Schema(description = "Có xóa cứng không (true = xóa vĩnh viễn, false = xóa mềm)", example = "false", defaultValue = "false")
    private boolean hardDelete = false;

    @Schema(description = "Lý do xóa bến xe", example = "Bến xe không còn hoạt động")
    private String reason;
}