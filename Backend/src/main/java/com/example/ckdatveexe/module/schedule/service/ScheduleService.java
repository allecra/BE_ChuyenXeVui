package com.example.ckdatveexe.module.schedule.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.schedule.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import com.example.ckdatveexe.shared.util.PaymentProviderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final StationRepository stationRepository;
    private final ScheduleBusRepository scheduleBusRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProviderUtil paymentProviderUtil;

    // ==================== USER METHODS ====================

    /**
     * Get all active schedules for users with pagination
     */
    public Page<ScheduleResponse> getAllActiveSchedulesForUser(ScheduleSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Schedule> schedules = scheduleRepository.findByStatus(ScheduleStatus.ACTIVE, pageable);
        return schedules.map(this::convertToUserResponse);
    }

    /**
     * Get schedule detail for user (only active schedules)
     */
    public ScheduleResponse getScheduleDetailForUser(Integer scheduleId) {
        Schedule schedule = scheduleRepository.findByIdAndStatus(scheduleId, ScheduleStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        // Check if departure time is in the future
        if (schedule.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lịch trình đã khởi hành");
        }

        return convertToDetailResponse(schedule, false); // false = user view
    }

    /**
     * Search schedules for users with complex filters
     */
    public Page<ScheduleResponse> searchSchedulesForUser(ScheduleSearchRequest request) {
        log.info("🔍 [SCHEDULE] Searching schedules for user with filters: startStation={}, endStation={}, date={}",
                request.getStartStationId(), request.getEndStationId(), request.getDepartureDate());

        Pageable pageable = createPageable(request);
        LocalDateTime currentTime = LocalDateTime.now();

        Page<Schedule> schedules = scheduleRepository.searchSchedulesForUser(
                currentTime,
                request.getStartStationId(),
                request.getEndStationId(),
                request.getDepartureDate(),
                request.getTimeFrom(),
                request.getTimeTo(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getBusCompanyId(),
                request.getBusType(),
                request.getMinSeats(),
                pageable);

        log.info("✅ [SCHEDULE] Found {} schedules matching user search criteria", schedules.getTotalElements());
        return schedules.map(this::convertToUserResponse);
    }

    // ==================== COMPANY METHODS ====================

    /**
     * Create new schedule (COMPANY only)
     */
    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreateRequest request, Integer busCompanyId) {
        log.info("🚌 [SCHEDULE] Creating new schedule for route {} with bus {}", request.getRouteId(),
                request.getBusId());

        // Validate route ownership and existence
        Route route = routeRepository.findByIdAndBusCompanyId(request.getRouteId(), busCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tuyến đường với ID: " + request.getRouteId()));

        if (route.getStatus() != RouteStatus.ACTIVE) {
            throw new IllegalArgumentException("Chỉ có thể tạo lịch trình cho tuyến đang hoạt động");
        }

        // Validate bus ownership and existence
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + request.getBusId()));

        if (!bus.getCompany().getId().equals(busCompanyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        if (bus.getStatus() != BusStatus.ACTIVE) {
            throw new IllegalArgumentException("Chỉ có thể gán xe đang hoạt động vào lịch trình");
        }

        // Validate stations exist and belong to the route
        validateStationsInRoute(request.getStartStationId(), request.getEndStationId(), request.getRouteId());

        // Check for time conflicts with existing schedules
        List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedules(
                request.getBusId(), request.getDepartureTime(), request.getArrivalTime(), 0);

        if (!conflictingSchedules.isEmpty()) {
            throw new IllegalArgumentException("Xe đã có lịch trình trùng thời gian từ " +
                    conflictingSchedules.get(0).getDepartureTime() + " đến " +
                    conflictingSchedules.get(0).getArrivalTime());
        }

        // Create schedule
        Schedule schedule = new Schedule();
        schedule.setRoute(route);
        schedule.setBus(bus);
        schedule.setDepartureTime(request.getDepartureTime());
        schedule.setArrivalTime(request.getArrivalTime());
        schedule.setStartStationId(request.getStartStationId());
        schedule.setEndStationId(request.getEndStationId());
        schedule.setPrice(request.getPrice());
        schedule.setAvailableSeat(bus.getCapacity());
        schedule.setTotalSeats(bus.getCapacity());
        schedule.setStatus(ScheduleStatus.ACTIVE);
        schedule.setNotes(request.getNotes());

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("✅ [SCHEDULE] Created new schedule with ID: {}", savedSchedule.getId());

        return convertToCompanyResponse(savedSchedule);
    }

    /**
     * Get all schedules for company
     */
    public Page<ScheduleResponse> getAllSchedulesForCompany(Integer busCompanyId, ScheduleSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Schedule> schedules = scheduleRepository.findByBusCompanyId(busCompanyId, pageable);
        return schedules.map(this::convertToCompanyResponse);
    }

    /**
     * Get schedule detail for company
     */
    public ScheduleResponse getScheduleDetailForCompany(Integer scheduleId, Integer busCompanyId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        // Validate ownership
        if (!schedule.getBus().getCompany().getId().equals(busCompanyId)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId);
        }

        return convertToDetailResponse(schedule, true); // true = company view
    }

    /**
     * Update schedule information
     */
    @Transactional
    public ScheduleResponse updateSchedule(Integer scheduleId, ScheduleUpdateRequest request, Integer busCompanyId) {
        log.info("🚌 [SCHEDULE] Updating schedule ID: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        // Validate ownership
        if (!schedule.getBus().getCompany().getId().equals(busCompanyId)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId);
        }

        // Check if schedule has sold tickets
        if (scheduleRepository.hasSoldTickets(scheduleId)) {
            throw new IllegalArgumentException("Không thể cập nhật lịch trình đã có vé được bán");
        }

        // Update fields if provided
        boolean timeChanged = false;

        if (request.getDepartureTime() != null) {
            schedule.setDepartureTime(request.getDepartureTime());
            timeChanged = true;
        }

        if (request.getArrivalTime() != null) {
            schedule.setArrivalTime(request.getArrivalTime());
            timeChanged = true;
        }

        // Check for time conflicts if time changed
        if (timeChanged) {
            List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedules(
                    schedule.getBus().getId(), schedule.getDepartureTime(), schedule.getArrivalTime(), scheduleId);

            if (!conflictingSchedules.isEmpty()) {
                throw new IllegalArgumentException("Xe đã có lịch trình trùng thời gian từ " +
                        conflictingSchedules.get(0).getDepartureTime() + " đến " +
                        conflictingSchedules.get(0).getArrivalTime());
            }
        }

        if (request.getStartStationId() != null || request.getEndStationId() != null) {
            Integer startStationId = request.getStartStationId() != null ? request.getStartStationId()
                    : schedule.getStartStationId();
            Integer endStationId = request.getEndStationId() != null ? request.getEndStationId()
                    : schedule.getEndStationId();

            validateStationsInRoute(startStationId, endStationId, schedule.getRoute().getId());

            if (request.getStartStationId() != null) {
                schedule.setStartStationId(request.getStartStationId());
            }
            if (request.getEndStationId() != null) {
                schedule.setEndStationId(request.getEndStationId());
            }
        }

        if (request.getPrice() != null) {
            schedule.setPrice(request.getPrice());
        }

        if (request.getStatus() != null) {
            schedule.setStatus(request.getStatus());
        }

        if (request.getNotes() != null) {
            schedule.setNotes(request.getNotes());
        }

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("✅ [SCHEDULE] Updated schedule with ID: {}", scheduleId);

        return convertToCompanyResponse(updatedSchedule);
    }

    /**
     * Search schedules for company
     */
    public Page<ScheduleResponse> searchSchedulesForCompany(Integer busCompanyId, ScheduleSearchRequest request) {
        log.info("🚌 [SCHEDULE] Searching schedules for company ID: {} with filters", busCompanyId);

        Pageable pageable = createPageable(request);

        Page<Schedule> schedules = scheduleRepository.searchSchedulesForCompany(
                busCompanyId,
                request.getRouteId(),
                request.getBusId(),
                request.getStartStationId(),
                request.getEndStationId(),
                request.getDepartureTimeFrom(),
                request.getDepartureTimeTo(),
                request.getArrivalTimeFrom(),
                request.getArrivalTimeTo(),
                request.getStatus(),
                request.getMinPrice(),
                request.getMaxPrice(),
                pageable);

        log.info("✅ [SCHEDULE] Found {} schedules for company search", schedules.getTotalElements());
        return schedules.map(this::convertToCompanyResponse);
    }

    /**
     * Cancel schedule
     */
    @Transactional
    public void cancelSchedule(Integer scheduleId, Integer busCompanyId) {
        log.info("🚌 [SCHEDULE] Cancelling schedule ID: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        // Validate ownership
        if (!schedule.getBus().getCompany().getId().equals(busCompanyId)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId);
        }

        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new IllegalArgumentException("Lịch trình đã được hủy trước đó");
        }

        // Check if schedule has sold tickets
        if (scheduleRepository.hasSoldTickets(scheduleId)) {
            throw new IllegalArgumentException("Không thể hủy lịch trình đã có vé được bán");
        }

        schedule.setStatus(ScheduleStatus.CANCELLED);
        scheduleRepository.save(schedule);

        log.info("✅ [SCHEDULE] Cancelled schedule with ID: {}", scheduleId);
    }

    // ==================== HELPER METHODS ====================

    private Pageable createPageable(ScheduleSearchRequest request) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private void validateStationsInRoute(Integer startStationId, Integer endStationId, Integer routeId) {
        // For now, just validate that stations exist
        // In a more complex implementation, you would validate that stations are part
        // of the route
        stationRepository.findById(startStationId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy bến xuất phát với ID: " + startStationId));

        stationRepository.findById(endStationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến đến với ID: " + endStationId));
    }

    private ScheduleResponse convertToUserResponse(Schedule schedule) {
        // Get active buses for this schedule
        List<ScheduleBus> activeBuses = scheduleBusRepository.findActiveBusesByScheduleId(schedule.getId());

        // For backward compatibility, use the first active bus or the legacy bus
        Bus primaryBus = null;
        if (!activeBuses.isEmpty()) {
            primaryBus = activeBuses.get(0).getBus();
        } else if (schedule.getBus() != null) {
            primaryBus = schedule.getBus();
        }

        if (primaryBus == null) {
            throw new IllegalStateException("Lịch trình không có xe nào được gán");
        }

        ScheduleResponse response = ScheduleResponse.forList(
                schedule.getId(),
                schedule.getRoute().getId(),
                schedule.getRoute().getRouteName(),
                primaryBus.getId(),
                primaryBus.getName(),
                primaryBus.getLicensePlate(),
                primaryBus.getBusType(),
                primaryBus.getCompany().getId(),
                primaryBus.getCompany().getCompanyName(),
                schedule.getDepartureTime(),
                schedule.getArrivalTime(),
                schedule.getStartStationId(),
                schedule.getStartStation() != null ? schedule.getStartStation().getName() : "Unknown",
                schedule.getStartStation() != null ? schedule.getStartStation().getLocation() : "Unknown",
                schedule.getEndStationId(),
                schedule.getEndStation() != null ? schedule.getEndStation().getName() : "Unknown",
                schedule.getEndStation() != null ? schedule.getEndStation().getLocation() : "Unknown",
                schedule.getPrice(),
                schedule.getAvailableSeat(),
                schedule.getTotalSeats(),
                schedule.getStatus(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt());

        // Add active bus count for user view
        response.setActiveBusCount(activeBuses.size());

        return response;
    }

    private ScheduleResponse convertToCompanyResponse(Schedule schedule) {
        ScheduleResponse response = convertToUserResponse(schedule);
        response.setNotes(schedule.getNotes());

        // Add all buses for company view
        List<ScheduleBus> allBuses = scheduleBusRepository.findAllBusesByScheduleId(schedule.getId());
        List<ScheduleBusResponse> busResponses = allBuses.stream()
                .map(this::convertScheduleBusToResponse)
                .toList();
        response.setBuses(busResponses);

        return response;
    }

    private ScheduleResponse convertToDetailResponse(Schedule schedule, boolean isCompanyView) {
        ScheduleResponse response = isCompanyView ? convertToCompanyResponse(schedule)
                : convertToUserResponse(schedule);

        if (!isCompanyView) {
            // For user detail view, show only active buses
            List<ScheduleBus> activeBuses = scheduleBusRepository.findActiveBusesByScheduleId(schedule.getId());
            List<ScheduleBusResponse> busResponses = activeBuses.stream()
                    .map(this::convertScheduleBusToResponse)
                    .toList();
            response.setBuses(busResponses);
        }

        return response;
    }

    private ScheduleBusResponse convertScheduleBusToResponse(ScheduleBus scheduleBus) {
        Bus bus = scheduleBus.getBus();
        return ScheduleBusResponse.from(
                scheduleBus.getId(),
                scheduleBus.getScheduleId(),
                scheduleBus.getBusId(),
                bus.getName(),
                bus.getLicensePlate(),
                bus.getBusType(),
                bus.getCapacity(),
                scheduleBus.getStatus(),
                scheduleBus.getCreatedAt());
    }

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

        return convertToCompanyResponse(savedSchedule);
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
        // Note: ScheduleBus entity doesn't have notes field

        scheduleBusRepository.save(scheduleBus);

        // Refresh schedule to get updated buses
        Schedule updatedSchedule = scheduleRepository.findById(scheduleId).get();

        log.info("Assigned bus {} to schedule {} by company {}",
                request.getBusId(), scheduleId, companyId);

        return convertToCompanyResponse(updatedSchedule);
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

        return convertToCompanyResponse(updatedSchedule);
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

        return convertToCompanyResponse(updatedSchedule);
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

    /**
     * Get popular routes based on booking count
     */
    public List<PopularRouteResponse> getPopularRoutes(int limit) {
        log.info("📊 [SCHEDULE] Getting top {} popular routes based on booking count", limit);

        List<Object[]> results = scheduleRepository.findPopularRoutesByBookingCount(limit);

        List<PopularRouteResponse> popularRoutes = results.stream()
                .map(result -> PopularRouteResponse.builder()
                        .routeId((Integer) result[0])
                        .routeName((String) result[1])
                        .startStationName((String) result[2])
                        .endStationName((String) result[3])
                        .startProvince((String) result[4])
                        .endProvince((String) result[5])
                        .totalBookings((Long) result[6])
                        .averagePrice((Double) result[7])
                        .totalSchedules((Integer) result[8])
                        .description((String) result[9])
                        .build())
                .collect(Collectors.toList());

        log.info("✅ [SCHEDULE] Found {} popular routes", popularRoutes.size());
        return popularRoutes;
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
}