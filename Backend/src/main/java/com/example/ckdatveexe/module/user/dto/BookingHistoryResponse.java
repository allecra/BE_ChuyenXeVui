package com.example.ckdatveexe.module.user.dto;

import com.example.ckdatveexe.shared.entity.Ticket;
import com.example.ckdatveexe.shared.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryResponse {
    private Integer ticketId;
    private String ticketCode;
    private String routeName;
    private String startStation;
    private String endStation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String seatNumber;
    private String seatType;
    private Double price;
    private TicketStatus status;
    private LocalDateTime bookingTime;
    private String busName;
    private String licensePlate;
    private String companyName;
    private String paymentStatus;
    private String paymentMethod;

    public static BookingHistoryResponse fromEntity(Ticket ticket) {
        return BookingHistoryResponse.builder()
                .ticketId(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .routeName(ticket.getSchedule().getRoute().getRouteName())
                .startStation("Bến xe khởi hành") // Mock data - cần implement logic thực tế
                .endStation("Bến xe đến") // Mock data - cần implement logic thực tế
                .departureTime(ticket.getSchedule().getDepartureTime())
                .arrivalTime(ticket.getSchedule().getArrivalTime())
                .seatNumber(ticket.getSeat().getSeatNumber())
                .seatType(ticket.getSeat().getSeatType().name())
                .price(ticket.getPrice())
                .status(ticket.getStatus())
                .bookingTime(ticket.getCreatedAt())
                .busName(ticket.getSchedule().getBus().getName())
                .licensePlate(ticket.getSchedule().getBus().getLicensePlate())
                .companyName(ticket.getSchedule().getBus().getCompany().getCompanyName())
                .paymentStatus(ticket.getPayments() != null && !ticket.getPayments().isEmpty()
                        ? ticket.getPayments().get(0).getStatus().name()
                        : "PENDING")
                .paymentMethod(ticket.getPayments() != null && !ticket.getPayments().isEmpty()
                        ? ticket.getPayments().get(0).getPaymentMethod().name()
                        : null)
                .build();
    }
}