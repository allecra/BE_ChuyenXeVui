package com.example.ckdatveexe.module.schedule.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.schedule.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import com.example.ckdatveexe.shared.util.PaymentProviderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleManagementService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleBusRepository scheduleBusRepository;
    private final BusRepository busRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProviderUtil paymentProviderUtil;

    @Transactional
    public ScheduleResponse cancelSchedule(Integer scheduleId, ScheduleCancelRequest request, Integer companyId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch trình không tồn tại"));

        // Verify ownership
        if (!schedule.getRoute().getBusCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy lịch trình này");
        }

        // Check if schedule can be cancelled
        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new IllegalArgumentException("Lịch trình đã được hủy trước đó");
        }

        LocalDateTime now = LocalDateTime.now();
        if (schedule.getDepartureTime().isBefore(now)) {
            throw new IllegalArgumentException("Không thể hủy lịch trình đã khởi hành");
        }

        // Update schedule status
        schedule.setStatus(ScheduleStatus.CANCELLED);
        // Store cancellation info in notes field
        String cancellationInfo = "Cancelled at " + now +
                (request.getCancellationReason() != null ? " - Reason: " + request.getCancellationReason() : "");
        schedule.setNotes(cancellationInfo);

        // Cancel all associated buses
        for (ScheduleBus scheduleBus : schedule.getScheduleBuses()) {
            scheduleBus.setStatus(ScheduleBusStatus.CANCELLED);
        }

        Schedule savedSchedule = scheduleRepository.save(schedule);

        // Handle tickets if requested
        if (request.getProcessRefunds()) {
            processTicketRefundsForCancelledSchedule(scheduleId, request.getCancellationReason());
        }

        if (request.getNotifyPassengers()) {
            notifyPassengersOfCancellation(scheduleId, request.getCancellationReason());
        }

        log.info("Cancelled schedule: {} by company: {} with reason: {}",
                scheduleId, companyId, request.getCancellationReason());

        return convertToResponse(savedSchedule);
    }

    @Transactional
    public ScheduleResponse assignBusToSchedule(Integer scheduleId, AssignBusToScheduleRequest request,
            Integer companyId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch trình không tồn tại"));

        // Verify ownership
        if (!schedule.getRoute().getBusCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Bạn không có quyền gán xe cho lịch trình này");
        }

        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Xe buýt không tồn tại"));

        // Verify bus belongs to company
        if (!bus.getBusCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe buýt không thuộc về công ty của bạn");
        }

        // Check if bus is already assigned to this schedule
        boolean alreadyAssigned = schedule.getScheduleBuses().stream()
                .anyMatch(sb -> sb.getBus().getId().equals(request.getBusId()) &&
                        sb.getStatus() != ScheduleBusStatus.CANCELLED);

        if (alreadyAssigned) {
            throw new IllegalArgumentException("Xe buýt đã được gán cho lịch trình này");
        }

        // Check bus availability for the schedule time
        boolean busConflict = scheduleBusRepository.existsByBusIdAndScheduleTimeConflict(
                request.getBusId(), schedule.getDepartureTime(), schedule.getArrivalTime());

        if (busConflict) {
            throw new IllegalArgumentException("Xe buýt đã có lịch trình khác trong thời gian này");
        }

        // Create new ScheduleBus assignment
        ScheduleBus scheduleBus = new ScheduleBus();
        scheduleBus.setSchedule(schedule);
        scheduleBus.setBus(bus);
        scheduleBus.setStatus(ScheduleBusStatus.ACTIVE);
        // Note: ScheduleBus entity doesn't have notes field, storing in schedule notes
        // instead

        scheduleBusRepository.save(scheduleBus);

        // Refresh schedule to get updated buses
        Schedule updatedSchedule = scheduleRepository.findById(scheduleId).get();

        log.info("Assigned bus {} to schedule {} by company {}",
                request.getBusId(), scheduleId, companyId);

        return convertToResponse(updatedSchedule);
    }

    @Transactional
    public ScheduleResponse updateBusStatusInSchedule(Integer scheduleId, Integer busId,
            UpdateBusStatusRequest request, Integer companyId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch trình không tồn tại"));

        // Verify ownership
        if (!schedule.getRoute().getBusCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Bạn không có quyền cập nhật lịch trình này");
        }

        ScheduleBus scheduleBus = scheduleBusRepository.findByScheduleIdAndBusId(scheduleId, busId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe buýt không được gán cho lịch trình này"));

        scheduleBus.setStatus(request.getStatus());
        // Note: ScheduleBus entity doesn't have notes field

        scheduleBusRepository.save(scheduleBus);

        // Refresh schedule
        Schedule updatedSchedule = scheduleRepository.findById(scheduleId).get();

        log.info("Updated bus {} status to {} in schedule {} by company {}",
                busId, request.getStatus(), scheduleId, companyId);

        return convertToResponse(updatedSchedule);
    }

    @Transactional
    public ScheduleResponse removeBusFromSchedule(Integer scheduleId, Integer busId, Integer companyId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch trình không tồn tại"));

        // Verify ownership
        if (!schedule.getRoute().getBusCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Bạn không có quyền cập nhật lịch trình này");
        }

        ScheduleBus scheduleBus = scheduleBusRepository.findByScheduleIdAndBusId(scheduleId, busId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe buýt không được gán cho lịch trình này"));

        // Check if there are confirmed tickets for this bus
        boolean hasConfirmedTickets = ticketRepository.existsByScheduleIdAndSeatBusIdAndStatus(
                scheduleId, busId, TicketStatus.CONFIRMED);

        if (hasConfirmedTickets) {
            throw new IllegalArgumentException("Không thể gỡ xe buýt có vé đã được xác nhận");
        }

        // Cancel the bus assignment
        scheduleBus.setStatus(ScheduleBusStatus.CANCELLED);
        scheduleBusRepository.save(scheduleBus);

        // Cancel any pending tickets for this bus
        List<Ticket> pendingTickets = ticketRepository.findByScheduleIdAndSeatBusIdAndStatus(
                scheduleId, busId, TicketStatus.PENDING);

        for (Ticket ticket : pendingTickets) {
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.setCancellationReason("Xe buýt bị gỡ khỏi lịch trình");
            ticket.setCancellationTime(LocalDateTime.now());

            // Release seat
            if (ticket.getSeat() != null) {
                ticket.getSeat().setStatus(SeatStatus.AVAILABLE);
            }
        }

        ticketRepository.saveAll(pendingTickets);

        // Refresh schedule
        Schedule updatedSchedule = scheduleRepository.findById(scheduleId).get();

        log.info("Removed bus {} from schedule {} by company {}",
                busId, scheduleId, companyId);

        return convertToResponse(updatedSchedule);
    }

    private void processTicketRefundsForCancelledSchedule(Integer scheduleId, String reason) {
        List<Ticket> confirmedTickets = ticketRepository.findByScheduleIdAndStatus(scheduleId, TicketStatus.CONFIRMED);

        for (Ticket ticket : confirmedTickets) {
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.setCancellationReason("Lịch trình bị hủy: " + reason);
            ticket.setCancellationTime(LocalDateTime.now());

            // Release seat
            if (ticket.getSeat() != null) {
                ticket.getSeat().setStatus(SeatStatus.AVAILABLE);
            }

            // Create refund record (100% refund for company cancellation)
            createRefundForCancelledTicket(ticket, BigDecimal.valueOf(ticket.getPrice()));
        }

        ticketRepository.saveAll(confirmedTickets);
        log.info("Processed refunds for {} tickets in cancelled schedule {}", confirmedTickets.size(), scheduleId);
    }

    private void notifyPassengersOfCancellation(Integer scheduleId, String reason) {
        // TODO: Implement email/SMS notification to passengers
        log.info("Notifying passengers of schedule {} cancellation: {}", scheduleId, reason);
    }

    private void createRefundForCancelledTicket(Ticket ticket, BigDecimal refundAmount) {
        Payment refundPayment = new Payment();
        refundPayment.setUser(ticket.getUser());
        refundPayment.setTicket(ticket);
        refundPayment.setAmount(refundAmount);
        refundPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        refundPayment.setPaymentProvider(paymentProviderUtil.getSystemProvider());
        refundPayment.setStatus(PaymentStatus.PENDING);
        refundPayment.setTransactionId("REFUND_SCHEDULE_" + ticket.getTicketCode() + "_" + System.currentTimeMillis());
        refundPayment.setDescription("Hoàn tiền do hủy lịch trình: " + ticket.getTicketCode());

        paymentRepository.save(refundPayment);
    }

    private ScheduleResponse convertToResponse(Schedule schedule) {
        ScheduleResponse response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setDepartureTime(schedule.getDepartureTime());
        response.setArrivalTime(schedule.getArrivalTime());
        response.setStatus(schedule.getStatus());
        response.setNotes(schedule.getNotes()); // This will contain cancellation info if cancelled

        if (schedule.getRoute() != null) {
            response.setRouteId(schedule.getRoute().getId());
            response.setRouteName(schedule.getRoute().getRouteName());
            // Use station locations instead of route departure/arrival locations
            if (schedule.getStartStation() != null) {
                response.setStartStationLocation(schedule.getStartStation().getLocation());
            }
            if (schedule.getEndStation() != null) {
                response.setEndStationLocation(schedule.getEndStation().getLocation());
            }
        }

        return response;
    }
}