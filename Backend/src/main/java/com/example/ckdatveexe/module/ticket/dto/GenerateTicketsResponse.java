package com.example.ckdatveexe.module.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả phát hành vé")
public class GenerateTicketsResponse {

    @Schema(description = "ID lịch trình", example = "1")
    private Integer scheduleId;

    @Schema(description = "Tổng số ghế", example = "40")
    private Integer totalSeats;

    @Schema(description = "Số vé đã tạo", example = "40")
    private Integer ticketsGenerated;

    @Schema(description = "Số vé có sẵn", example = "40")
    private Integer availableTickets;

    @Schema(description = "Số vé đã đặt", example = "0")
    private Integer bookedTickets;

    @Schema(description = "Thông báo", example = "Phát hành vé thành công")
    private String message;
}