package com.example.ckdatveexe.module.ticket.dto;

import com.example.ckdatveexe.shared.entity.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminTicketSearchRequest {
    private String ticketCode;
    private String passengerName;
    private String passengerPhone;
    private String passengerEmail;
    private Integer userId;
    private Integer scheduleId;
    private Integer routeId;
    private Integer busCompanyId;
    private TicketStatus status;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Double minPrice;
    private Double maxPrice;
}