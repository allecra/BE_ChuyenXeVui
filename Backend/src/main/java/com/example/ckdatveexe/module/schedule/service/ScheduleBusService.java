package com.example.ckdatveexe.module.schedule.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.schedule.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleBusService {

    private final ScheduleBusRepository scheduleBusRepository;
    private final ScheduleRepository scheduleRepository;
    private final BusRepository busRepository;

    /**
     * Assign buses to a schedule
     */
    @Transactional
    public List<ScheduleBusResponse> assignBusesToSchedule(Integer scheduleId, ScheduleBusRequest request,
            Integer busCompanyId) {
        log.info("🚌 [SCHEDULE-BUS] Assigning {} buses to schedule {}", request.getBusIds().size(), scheduleId);

        // Validate schedule ownership and existence
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        if (!schedule.getRoute().getBusCompany().getId().equals(busCompanyId)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId);
        }

        List<ScheduleBus> scheduleBuses = new ArrayList<>();

        for (Integer busId : request.getBusIds()) {
            // Validate bus ownership and existence
            Bus bus = busRepository.findById(busId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + busId));

            if (!bus.getCompany().getId().equals(busCompanyId)) {
                throw new IllegalArgumentException("Xe ID " + busId + " không thuộc về nhà xe này");
            }

            if (bus.getStatus() != BusStatus.ACTIVE) {
                throw new IllegalArgumentException("Xe ID " + busId + " không đang hoạt động");
            }

            // Check if bus is already assigned to this schedule
            if (scheduleBusRepository.existsByScheduleIdAndBusId(scheduleId, busId)) {
                throw new IllegalArgumentException("Xe ID " + busId + " đã được gán vào lịch trình này");
            }

            // Check for time conflicts
            List<ScheduleBus> conflictingScheduleBuses = scheduleBusRepository.findConflictingScheduleBuses(
                    busId, schedule.getDepartureTime(), schedule.getArrivalTime(), scheduleId);

            if (!conflictingScheduleBuses.isEmpty()) {
                ScheduleBus conflicting = conflictingScheduleBuses.get(0);
                throw new IllegalArgumentException("Xe ID " + busId + " đã có lịch trình trùng thời gian từ " +
                        conflicting.getSchedule().getDepartureTime() + " đến " +
                        conflicting.getSchedule().getArrivalTime());
            }

            // Create ScheduleBus
            ScheduleBus scheduleBus = new ScheduleBus();
            scheduleBus.setScheduleId(scheduleId);
            scheduleBus.setBusId(busId);
            scheduleBus.setStatus(ScheduleBusStatus.ACTIVE);

            scheduleBuses.add(scheduleBus);
        }

        // Save all schedule buses
        List<ScheduleBus> savedScheduleBuses = scheduleBusRepository.saveAll(scheduleBuses);

        // Update schedule total seats
        updateScheduleTotalSeats(scheduleId);

        log.info("✅ [SCHEDULE-BUS] Successfully assigned {} buses to schedule {}",
                savedScheduleBuses.size(), scheduleId);

        return savedScheduleBuses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Remove bus from schedule
     */
    @Transactional
    public void removeBusFromSchedule(Integer scheduleId, Integer busId, Integer busCompanyId) {
        log.info("🚌 [SCHEDULE-BUS] Removing bus {} from schedule {}", busId, scheduleId);

        // Validate schedule ownership
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        if (!schedule.getRoute().getBusCompany().getId().equals(busCompanyId)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId);
        }

        // Check if ScheduleBus exists
        ScheduleBus scheduleBus = scheduleBusRepository.findByScheduleIdAndBusId(scheduleId, busId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe không được gán vào lịch trình này"));

        // Check if bus has sold tickets
        if (scheduleBusRepository.hasSoldTicketsForBusInSchedule(busId, scheduleId)) {
            throw new IllegalArgumentException("Không thể gỡ xe đã có vé được bán trong lịch trình");
        }

        // Remove the assignment
        scheduleBusRepository.delete(scheduleBus);

        // Update schedule total seats
        updateScheduleTotalSeats(scheduleId);

        log.info("✅ [SCHEDULE-BUS] Successfully removed bus {} from schedule {}", busId, scheduleId);
    }

    /**
     * Get all buses in a schedule (for company view)
     */
    public List<ScheduleBusResponse> getBusesInSchedule(Integer scheduleId, Integer busCompanyId) {
        // Validate schedule ownership
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        if (!schedule.getRoute().getBusCompany().getId().equals(busCompanyId)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId);
        }

        List<ScheduleBus> scheduleBuses = scheduleBusRepository.findAllBusesByScheduleId(scheduleId);
        return scheduleBuses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active buses in a schedule (for user view)
     */
    public List<ScheduleBusResponse> getActiveBusesInSchedule(Integer scheduleId) {
        List<ScheduleBus> scheduleBuses = scheduleBusRepository.findActiveBusesByScheduleId(scheduleId);
        return scheduleBuses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update bus status in schedule
     */
    @Transactional
    public ScheduleBusResponse updateBusStatusInSchedule(Integer scheduleId, Integer busId,
            ScheduleBusStatusUpdateRequest request,
            Integer busCompanyId) {
        log.info("🚌 [SCHEDULE-BUS] Updating bus {} status in schedule {} to {}",
                busId, scheduleId, request.getStatus());

        // Validate schedule ownership
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        if (!schedule.getRoute().getBusCompany().getId().equals(busCompanyId)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId);
        }

        // Find ScheduleBus
        ScheduleBus scheduleBus = scheduleBusRepository.findByScheduleIdAndBusId(scheduleId, busId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe không được gán vào lịch trình này"));

        // Update status
        scheduleBus.setStatus(request.getStatus());
        ScheduleBus updatedScheduleBus = scheduleBusRepository.save(scheduleBus);

        // Update schedule total seats
        updateScheduleTotalSeats(scheduleId);

        log.info("✅ [SCHEDULE-BUS] Updated bus {} status in schedule {} to {}",
                busId, scheduleId, request.getStatus());

        return convertToResponse(updatedScheduleBus);
    }

    /**
     * Update schedule total seats based on active buses
     */
    private void updateScheduleTotalSeats(Integer scheduleId) {
        List<ScheduleBus> activeBuses = scheduleBusRepository.findActiveBusesByScheduleId(scheduleId);
        int totalSeats = activeBuses.stream()
                .mapToInt(sb -> sb.getBus().getCapacity())
                .sum();

        Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
        if (schedule != null) {
            schedule.setTotalSeats(totalSeats);
            schedule.setAvailableSeat(totalSeats); // Reset available seats
            scheduleRepository.save(schedule);
        }
    }

    /**
     * Convert ScheduleBus to Response
     */
    private ScheduleBusResponse convertToResponse(ScheduleBus scheduleBus) {
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