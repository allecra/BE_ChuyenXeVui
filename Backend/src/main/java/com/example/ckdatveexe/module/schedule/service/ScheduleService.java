package com.example.ckdatveexe.module.schedule.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.schedule.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final StationRepository stationRepository;
    private final ScheduleBusRepository scheduleBusRepository;

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
}