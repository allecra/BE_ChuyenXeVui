package com.example.ckdatveexe.module.ticket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class TicketReportResponse {
    private LocalDateTime reportDate;
    private Integer totalTickets;
    private Integer confirmedTickets;
    private Integer cancelledTickets;
    private Integer pendingTickets;
    private Integer expiredTickets;
    private BigDecimal totalRevenue;
    private BigDecimal confirmedRevenue;
    private BigDecimal refundedAmount;
    private Map<String, Integer> ticketsByStatus;
    private Map<String, BigDecimal> revenueByStatus;
    private Map<String, Integer> ticketsByRoute;
    private Map<String, Integer> ticketsByCompany;
    private BigDecimal averageTicketPrice;
    private Integer totalPassengers;
}