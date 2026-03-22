package com.example.ckdatveexe.module.ticket.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.ticket.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminTicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final RouteRepository routeRepository;
    private final BusCompanyRepository busCompanyRepository;

    public Page<TicketResponse> searchTickets(AdminTicketSearchRequest searchRequest, Pageable pageable) {
        Specification<Ticket> spec = createSearchSpecification(searchRequest);
        Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);

        return tickets.map(TicketResponse::fromEntity);
    }

    public TicketResponse getTicketDetail(Integer ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Vé không tồn tại"));

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    public TicketResponse updateTicketStatus(Integer ticketId, TicketStatus newStatus, String reason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Vé không tồn tại"));

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);

        // Add reason to notes if provided
        if (reason != null && !reason.trim().isEmpty()) {
            String currentNotes = ticket.getNotes() != null ? ticket.getNotes() : "";
            ticket.setNotes(currentNotes + "\n[ADMIN] " + LocalDateTime.now() + ": " + reason);
        }

        // Handle seat status changes
        if (newStatus == TicketStatus.CANCELLED && oldStatus == TicketStatus.CONFIRMED) {
            // Release seat when cancelling confirmed ticket
            if (ticket.getSeat() != null) {
                ticket.getSeat().setStatus(SeatStatus.AVAILABLE);
            }
        } else if (newStatus == TicketStatus.CONFIRMED && oldStatus != TicketStatus.CONFIRMED) {
            // Book seat when confirming ticket
            if (ticket.getSeat() != null) {
                ticket.getSeat().setStatus(SeatStatus.BOOKED);
            }
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        log.info("Admin updated ticket {} status from {} to {} with reason: {}",
                ticketId, oldStatus, newStatus, reason);

        return TicketResponse.fromEntity(updatedTicket);
    }

    public TicketReportResponse generateTicketReport(LocalDateTime fromDate, LocalDateTime toDate) {
        List<Ticket> tickets;

        if (fromDate != null && toDate != null) {
            tickets = ticketRepository.findByCreatedAtBetween(fromDate, toDate);
        } else {
            // Default to last 30 days
            LocalDateTime defaultFromDate = LocalDateTime.now().minusDays(30);
            LocalDateTime defaultToDate = LocalDateTime.now();
            tickets = ticketRepository.findByCreatedAtBetween(defaultFromDate, defaultToDate);
        }

        TicketReportResponse report = new TicketReportResponse();
        report.setReportDate(LocalDateTime.now());
        report.setTotalTickets(tickets.size());

        // Count by status
        Map<TicketStatus, Long> statusCounts = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));

        report.setConfirmedTickets(statusCounts.getOrDefault(TicketStatus.CONFIRMED, 0L).intValue());
        report.setCancelledTickets(statusCounts.getOrDefault(TicketStatus.CANCELLED, 0L).intValue());
        report.setPendingTickets(statusCounts.getOrDefault(TicketStatus.PENDING, 0L).intValue());
        report.setExpiredTickets(statusCounts.getOrDefault(TicketStatus.EXPIRED, 0L).intValue());

        // Revenue calculations
        BigDecimal totalRevenue = tickets.stream()
                .map(t -> BigDecimal.valueOf(t.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRevenue(totalRevenue);

        BigDecimal confirmedRevenue = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CONFIRMED)
                .map(t -> BigDecimal.valueOf(t.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setConfirmedRevenue(confirmedRevenue);

        // Calculate refunded amount (cancelled tickets)
        BigDecimal refundedAmount = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CANCELLED)
                .map(t -> BigDecimal.valueOf(t.getPrice() * 0.8)) // Assuming 80% refund
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setRefundedAmount(refundedAmount);

        // Status distribution
        Map<String, Integer> ticketsByStatus = statusCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        e -> e.getValue().intValue()));
        report.setTicketsByStatus(ticketsByStatus);

        // Revenue by status
        Map<String, BigDecimal> revenueByStatus = new HashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            BigDecimal revenue = tickets.stream()
                    .filter(t -> t.getStatus() == status)
                    .map(t -> BigDecimal.valueOf(t.getPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            revenueByStatus.put(status.name(), revenue);
        }
        report.setRevenueByStatus(revenueByStatus);

        // Tickets by route
        Map<String, Integer> ticketsByRoute = tickets.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getSchedule().getRoute().getDepartureLocation() + " - " +
                                t.getSchedule().getRoute().getArrivalLocation(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        report.setTicketsByRoute(ticketsByRoute);

        // Tickets by company
        Map<String, Integer> ticketsByCompany = tickets.stream()
                .filter(t -> t.getSchedule().getRoute().getBusCompany() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getSchedule().getRoute().getBusCompany().getCompanyName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        report.setTicketsByCompany(ticketsByCompany);

        // Average ticket price
        if (!tickets.isEmpty()) {
            report.setAverageTicketPrice(
                    totalRevenue.divide(BigDecimal.valueOf(tickets.size()), 2, RoundingMode.HALF_UP));
        } else {
            report.setAverageTicketPrice(BigDecimal.ZERO);
        }

        // Total passengers (confirmed tickets only)
        report.setTotalPassengers(report.getConfirmedTickets());

        return report;
    }

    public List<TicketResponse> getExpiredTickets() {
        List<Ticket> expiredTickets = ticketRepository.findByStatus(TicketStatus.EXPIRED);
        return expiredTickets.stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cleanupExpiredTickets() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24); // Cleanup tickets expired more than 24h ago

        List<Ticket> expiredTickets = ticketRepository.findExpiredTicketsOlderThan(cutoffTime);

        for (Ticket ticket : expiredTickets) {
            // Release seat if still locked
            if (ticket.getSeat() != null && ticket.getSeat().getStatus() == SeatStatus.LOCKED) {
                ticket.getSeat().setStatus(SeatStatus.AVAILABLE);
            }
        }

        log.info("Cleaned up {} expired tickets older than {}", expiredTickets.size(), cutoffTime);
    }

    private Specification<Ticket> createSearchSpecification(AdminTicketSearchRequest searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchRequest.getTicketCode() != null && !searchRequest.getTicketCode().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("ticketCode")),
                        "%" + searchRequest.getTicketCode().toLowerCase() + "%"));
            }

            if (searchRequest.getPassengerName() != null && !searchRequest.getPassengerName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("passengerName")),
                        "%" + searchRequest.getPassengerName().toLowerCase() + "%"));
            }

            if (searchRequest.getPassengerPhone() != null && !searchRequest.getPassengerPhone().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("passengerPhone"),
                        "%" + searchRequest.getPassengerPhone() + "%"));
            }

            if (searchRequest.getPassengerEmail() != null && !searchRequest.getPassengerEmail().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("passengerEmail")),
                        "%" + searchRequest.getPassengerEmail().toLowerCase() + "%"));
            }

            if (searchRequest.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), searchRequest.getUserId()));
            }

            if (searchRequest.getScheduleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("schedule").get("id"), searchRequest.getScheduleId()));
            }

            if (searchRequest.getRouteId() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("schedule").get("route").get("id"), searchRequest.getRouteId()));
            }

            if (searchRequest.getBusCompanyId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("schedule").get("route").get("busCompany").get("id"),
                        searchRequest.getBusCompanyId()));
            }

            if (searchRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchRequest.getStatus()));
            }

            if (searchRequest.getFromDate() != null) {
                predicates
                        .add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), searchRequest.getFromDate()));
            }

            if (searchRequest.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), searchRequest.getToDate()));
            }

            if (searchRequest.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), searchRequest.getMinPrice()));
            }

            if (searchRequest.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), searchRequest.getMaxPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}