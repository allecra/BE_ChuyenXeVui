package com.example.ckdatveexe.module.ticket.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.ticket.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import com.example.ckdatveexe.shared.util.PaymentProviderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketCancellationService {

    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProviderUtil paymentProviderUtil;

    @Transactional
    public TicketCancellationResponse cancelTicket(TicketCancellationRequest request, Integer userId) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Vé không tồn tại"));

        // Verify ownership
        if (!ticket.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy vé này");
        }

        // Check if ticket can be cancelled
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalArgumentException("Vé đã được hủy trước đó");
        }

        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể hủy vé đã được xác nhận");
        }

        // Check cancellation time limit
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = ticket.getSchedule().getDepartureTime();

        // Get cancellation policy for the route
        CancellationPolicy policy = cancellationPolicyRepository.findByRouteId(ticket.getSchedule().getRoute().getId())
                .orElse(getDefaultCancellationPolicy());

        long hoursUntilDeparture = ChronoUnit.HOURS.between(now, departureTime);

        if (hoursUntilDeparture < policy.getCancellationTimeLimit()) {
            throw new IllegalArgumentException(
                    String.format("Không thể hủy vé. Cần hủy trước %d giờ so với giờ khởi hành",
                            policy.getCancellationTimeLimit()));
        }

        // Calculate refund amount
        BigDecimal originalAmount = BigDecimal.valueOf(ticket.getPrice());
        BigDecimal refundPercentage = BigDecimal.valueOf(policy.getRefundPercentage());
        BigDecimal refundAmount = originalAmount.multiply(refundPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal cancellationFee = originalAmount.subtract(refundAmount);

        // Update ticket status
        ticket.setStatus(TicketStatus.CANCELLED);
        // Store cancellation info in notes since entity doesn't have specific fields
        String cancellationInfo = String.format("[CANCELLED] %s - Reason: %s",
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                request.getCancellationReason());
        ticket.setNotes(ticket.getNotes() != null ? ticket.getNotes() + "\n" + cancellationInfo : cancellationInfo);

        ticketRepository.save(ticket);

        // Release seat
        Seat seat = ticket.getSeat();
        if (seat != null) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
        }

        // Create refund record (if refund amount > 0)
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            createRefundRecord(ticket, refundAmount, request);
        }

        log.info("Cancelled ticket: {} for user: {}, refund amount: {}",
                ticket.getTicketCode(), userId, refundAmount);

        // Prepare response
        TicketCancellationResponse response = new TicketCancellationResponse();
        response.setTicketId(ticket.getId());
        response.setTicketCode(ticket.getTicketCode());
        response.setOriginalAmount(originalAmount);
        response.setRefundAmount(refundAmount);
        response.setCancellationFee(cancellationFee);
        response.setRefundPercentage(policy.getRefundPercentage());
        response.setCancellationReason(request.getCancellationReason());
        response.setCancellationTime(now);
        response.setEstimatedRefundTime(now.plusDays(7)); // 7 days processing time
        response.setRefundMethod("Chuyển khoản ngân hàng");
        response.setRefundStatus("Đang xử lý");
        response.setMessage("Hủy vé thành công. Tiền hoàn sẽ được chuyển trong vòng 7 ngày làm việc.");

        return response;
    }

    @Transactional
    public TicketModificationResponse modifyTicket(TicketModificationRequest request, Integer userId) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Vé không tồn tại"));

        // Verify ownership
        if (!ticket.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền đổi vé này");
        }

        // Check if ticket can be modified
        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể đổi vé đã được xác nhận");
        }

        // Check modification time limit (24 hours before departure)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = ticket.getSchedule().getDepartureTime();
        long hoursUntilDeparture = ChronoUnit.HOURS.between(now, departureTime);

        if (hoursUntilDeparture < 24) {
            throw new IllegalArgumentException("Không thể đổi vé. Cần đổi trước 24 giờ so với giờ khởi hành");
        }

        Schedule oldSchedule = ticket.getSchedule();
        Schedule newSchedule = null;

        if (request.getNewScheduleId() != null) {
            newSchedule = scheduleRepository.findById(request.getNewScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lịch trình mới không tồn tại"));
        }

        // Store old information using entity fields
        String oldScheduleInfo = String.format("%s - %s (%s)",
                oldSchedule.getRoute().getStartLocation(),
                oldSchedule.getRoute().getEndLocation(),
                oldSchedule.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        String oldSeatNumber = ticket.getSeat() != null ? ticket.getSeat().getSeatNumber() : "";

        BigDecimal originalAmount = BigDecimal.valueOf(ticket.getPrice());
        BigDecimal newAmount = originalAmount;
        BigDecimal additionalFee = BigDecimal.ZERO;

        // Handle schedule change
        if (newSchedule != null) {
            // Release old seat
            Seat oldSeat = ticket.getSeat();
            if (oldSeat != null) {
                oldSeat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(oldSeat);
            }

            // Book new seat if specified
            if (request.getNewSeatNumbers() != null && !request.getNewSeatNumbers().isEmpty()) {
                String newSeatNumber = request.getNewSeatNumbers().get(0); // Take first seat

                // Find seat in the new schedule's buses
                Seat newSeat = null;
                if (newSchedule.getScheduleBuses() != null && !newSchedule.getScheduleBuses().isEmpty()) {
                    newSeat = seatRepository.findByBusIdAndSeatNumber(
                            newSchedule.getScheduleBuses().get(0).getBus().getId(),
                            newSeatNumber)
                            .orElse(null);
                } else if (newSchedule.getBus() != null) {
                    // Fallback to legacy single bus
                    newSeat = seatRepository.findByBusIdAndSeatNumber(
                            newSchedule.getBus().getId(),
                            newSeatNumber)
                            .orElse(null);
                }

                if (newSeat == null) {
                    throw new ResourceNotFoundException("Ghế " + newSeatNumber + " không tồn tại");
                }

                if (newSeat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new IllegalArgumentException("Ghế " + newSeatNumber + " không khả dụng");
                }

                newSeat.setStatus(SeatStatus.BOOKED);
                seatRepository.save(newSeat);

                // Update ticket with new seat and schedule
                ticket.setSeat(newSeat);
                ticket.setSchedule(newSchedule);
                ticket.setDepartureTime(newSchedule.getDepartureTime());
                ticket.setArrivalTime(newSchedule.getArrivalTime());

                // Calculate new price
                newAmount = BigDecimal.valueOf(calculatePrice(newSeat, newSchedule));
            }
        }

        // Calculate modification fee (5% of original amount)
        BigDecimal modificationFee = originalAmount.multiply(BigDecimal.valueOf(0.05));
        additionalFee = additionalFee.add(modificationFee);

        // If new amount is higher, add the difference
        if (newAmount.compareTo(originalAmount) > 0) {
            additionalFee = additionalFee.add(newAmount.subtract(originalAmount));
        }

        // Update ticket
        ticket.setPrice(newAmount.doubleValue());

        // Store modification info in notes
        String modificationInfo = String.format("[MODIFIED] %s - Reason: %s",
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                request.getModificationReason());
        ticket.setNotes(ticket.getNotes() != null ? ticket.getNotes() + "\n" + modificationInfo : modificationInfo);

        ticketRepository.save(ticket);

        // Prepare response
        TicketModificationResponse response = new TicketModificationResponse();
        response.setTicketId(ticket.getId());
        response.setTicketCode(ticket.getTicketCode());
        response.setOriginalAmount(originalAmount);
        response.setNewAmount(newAmount);
        response.setAdditionalFee(additionalFee);
        response.setModificationReason(request.getModificationReason());
        response.setModificationTime(now);
        response.setOldScheduleInfo(oldScheduleInfo);

        if (newSchedule != null) {
            response.setNewScheduleInfo(String.format("%s - %s (%s)",
                    newSchedule.getRoute().getStartLocation(),
                    newSchedule.getRoute().getEndLocation(),
                    newSchedule.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }

        response.setOldSeatNumbers(oldSeatNumber);
        if (request.getNewSeatNumbers() != null && !request.getNewSeatNumbers().isEmpty()) {
            response.setNewSeatNumbers(String.join(", ", request.getNewSeatNumbers()));
        }

        response.setMessage("Đổi vé thành công. Phí đổi vé: " + additionalFee + " VND");

        log.info("Modified ticket: {} for user: {}, additional fee: {}",
                ticket.getTicketCode(), userId, additionalFee);

        return response;
    }

    private CancellationPolicy getDefaultCancellationPolicy() {
        CancellationPolicy defaultPolicy = new CancellationPolicy();
        defaultPolicy.setCancellationTimeLimit(24); // 24 hours
        defaultPolicy.setRefundPercentage(80); // 80% refund
        defaultPolicy.setDescriptions("Chính sách hủy vé mặc định: Hủy trước 24h được hoàn 80%");
        return defaultPolicy;
    }

    private void createRefundRecord(Ticket ticket, BigDecimal refundAmount, TicketCancellationRequest request) {
        // Create a payment record for refund tracking
        Payment refundPayment = new Payment();
        refundPayment.setUser(ticket.getUser());
        refundPayment.setTicket(ticket);
        refundPayment.setAmount(refundAmount);
        refundPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        refundPayment.setPaymentProvider(paymentProviderUtil.getSystemProvider());
        refundPayment.setStatus(PaymentStatus.PENDING);
        refundPayment.setTransactionId("REFUND_" + ticket.getTicketCode() + "_" + System.currentTimeMillis());
        refundPayment.setDescription("Hoàn tiền hủy vé: " + ticket.getTicketCode());

        // Store bank account info for refund in callback data
        if (request.getBankAccountNumber() != null) {
            String bankInfo = String.format(
                    "Bank: %s, Account: %s, Name: %s",
                    request.getBankName(),
                    request.getBankAccountNumber(),
                    request.getBankAccountName());
            refundPayment.setCallbackData(bankInfo);
        }

        paymentRepository.save(refundPayment);
        log.info("Created refund record: {} for amount: {}", refundPayment.getTransactionId(), refundAmount);
    }

    private Double calculatePrice(Seat seat, Schedule schedule) {
        // Base price from seat
        Double basePrice = seat.getPriceForSeatType();
        // TODO: Add route-based pricing logic if needed
        return basePrice;
    }
}