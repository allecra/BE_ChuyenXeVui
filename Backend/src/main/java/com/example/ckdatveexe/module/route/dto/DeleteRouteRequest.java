package com.example.ckdatveexe.module.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để xóa tuyến đường")
public class DeleteRouteRequest {

    @Schema(description = "Xóa cứng (true) hoặc xóa mềm (false)", example = "false")
    private boolean hardDelete = false;

    @Schema(description = "Lý do xóa tuyến", example = "Tuyến không còn hiệu quả kinh doanh")
    private String reason;
}