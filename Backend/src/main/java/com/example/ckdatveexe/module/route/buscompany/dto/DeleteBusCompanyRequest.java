package com.example.ckdatveexe.module.route.buscompany.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request để xóa nhà xe")
public class DeleteBusCompanyRequest {

    @Schema(description = "Có xóa cứng không (true = xóa vĩnh viễn, false = tạm khóa tài khoản)", example = "false", defaultValue = "false")
    private boolean hardDelete = false;

    @Schema(description = "Lý do xóa/khóa nhà xe", example = "Vi phạm chính sách hoặc yêu cầu từ nhà xe")
    private String reason;
}