package com.example.ckdatveexe.module.seat.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.seat.dto.*;
import com.example.ckdatveexe.shared.entity.Bus;
import com.example.ckdatveexe.shared.entity.Seat;
import com.example.ckdatveexe.shared.entity.SeatStatus;
import com.example.ckdatveexe.shared.repository.BusRepository;
import com.example.ckdatveexe.shared.repository.SeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final BusRepository busRepository;

    // ==================== USER METHODS ====================

    /**
     * Get seats for a bus (USER view - exclude DELETED seats)
     */
    public Page<SeatResponse> getSeatsForUser(Integer busId, SeatSearchRequest request) {
        // Verify bus exists
        if (!busRepository.existsById(busId)) {
            throw new ResourceNotFoundException("Không tìm thấy xe với ID: " + busId);
        }

        Pageable pageable = createPageable(request);
        Page<Seat> seats = seatRepository.searchSeatsForUser(
                busId,
                request.getStatus(),
                request.getSeatType(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getSeatNumber(),
                pageable);

        return seats.map(this::convertToUserResponse);
    }

    /**
     * Get seat detail for user (exclude DELETED seats)
     */
    public SeatResponse getSeatDetailForUser(Integer seatId) {
        Seat seat = seatRepository.findByIdForUser(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế với ID: " + seatId));

        return convertToUserResponse(seat);
    }

    // ==================== BUS_COMPANY METHODS ====================

    /**
     * Create new seat (BUS_COMPANY only)
     */
    @Transactional
    public SeatResponse createSeat(Integer companyId, SeatCreateRequest request) {
        // Verify bus belongs to company
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + request.getBusId()));

        if (!bus.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        // Check if seat number already exists in this bus
        if (seatRepository.existsByBusIdAndSeatNumber(request.getBusId(), request.getSeatNumber())) {
            throw new IllegalArgumentException("Số ghế đã tồn tại trong xe này");
        }

        Seat seat = new Seat();
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());
        seat.setPriceForSeatType(request.getPriceForSeatType());
        seat.setRowNumber(request.getRowNumber());
        seat.setColumnNumber(request.getColumnNumber());
        seat.setBus(bus);
        seat.setStatus(SeatStatus.AVAILABLE);

        Seat savedSeat = seatRepository.save(seat);
        log.info("Created new seat: {} for bus: {} by company: {}",
                savedSeat.getSeatNumber(), bus.getName(), companyId);

        return convertToCompanyResponse(savedSeat);
    }

    /**
     * Get seats for company (include all statuses)
     */
    public Page<SeatResponse> getSeatsForCompany(Integer companyId, SeatSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Seat> seats = seatRepository.searchSeatsForCompany(
                request.getBusId(),
                companyId,
                request.getStatus(),
                request.getSeatType(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getSeatNumber(),
                pageable);

        return seats.map(this::convertToCompanyResponse);
    }

    /**
     * Get seat detail for company
     */
    public SeatResponse getSeatDetailForCompany(Integer companyId, Integer seatId) {
        Seat seat = seatRepository.findByIdAndCompanyId(seatId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế với ID: " + seatId));

        return convertToCompanyResponse(seat);
    }

    /**
     * Update seat information (BUS_COMPANY only)
     */
    @Transactional
    public SeatResponse updateSeat(Integer companyId, Integer seatId, SeatUpdateRequest request) {
        Seat seat = seatRepository.findByIdAndCompanyId(seatId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế với ID: " + seatId));

        // Check if seat is BOOKED - cannot update BOOKED seats
        if (seat.getStatus() == SeatStatus.BOOKED) {
            throw new IllegalArgumentException("Không thể cập nhật ghế đã được đặt");
        }

        // Update fields if provided
        if (request.getSeatNumber() != null && !request.getSeatNumber().trim().isEmpty()) {
            // Check if new seat number already exists (excluding current seat)
            if (seatRepository.existsByBusIdAndSeatNumberAndIdNot(
                    seat.getBus().getId(), request.getSeatNumber(), seatId)) {
                throw new IllegalArgumentException("Số ghế đã tồn tại trong xe này");
            }
            seat.setSeatNumber(request.getSeatNumber());
        }

        if (request.getSeatType() != null) {
            seat.setSeatType(request.getSeatType());
        }

        if (request.getPriceForSeatType() != null) {
            seat.setPriceForSeatType(request.getPriceForSeatType());
        }

        if (request.getRowNumber() != null) {
            seat.setRowNumber(request.getRowNumber());
        }

        if (request.getColumnNumber() != null) {
            seat.setColumnNumber(request.getColumnNumber());
        }

        Seat updatedSeat = seatRepository.save(seat);
        log.info("Updated seat: {} for company: {}", updatedSeat.getSeatNumber(), companyId);

        return convertToCompanyResponse(updatedSeat);
    }

    /**
     * Update seat status (BUS_COMPANY only)
     */
    @Transactional
    public SeatResponse updateSeatStatus(Integer companyId, Integer seatId, SeatStatusUpdateRequest request) {
        Seat seat = seatRepository.findByIdAndCompanyId(seatId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế với ID: " + seatId));

        // Validate status transition
        validateStatusTransition(seat.getStatus(), request.getStatus());

        seat.setStatus(request.getStatus());
        Seat updatedSeat = seatRepository.save(seat);

        log.info("Updated seat status: {} from {} to {} for company: {}",
                seat.getSeatNumber(), seat.getStatus(), request.getStatus(), companyId);

        return convertToCompanyResponse(updatedSeat);
    }

    /**
     * Update seat price (BUS_COMPANY only)
     */
    @Transactional
    public SeatResponse updateSeatPrice(Integer companyId, Integer seatId, SeatPriceUpdateRequest request) {
        Seat seat = seatRepository.findByIdAndCompanyId(seatId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế với ID: " + seatId));

        // Check if seat is BOOKED - cannot update price of BOOKED seats
        if (seat.getStatus() == SeatStatus.BOOKED) {
            throw new IllegalArgumentException("Không thể thay đổi giá ghế đã được đặt");
        }

        seat.setPriceForSeatType(request.getPriceForSeatType());
        Seat updatedSeat = seatRepository.save(seat);

        log.info("Updated seat price: {} to {} for company: {}",
                seat.getSeatNumber(), request.getPriceForSeatType(), companyId);

        return convertToCompanyResponse(updatedSeat);
    }

    /**
     * Soft delete seat (BUS_COMPANY only)
     */
    @Transactional
    public void deleteSeat(Integer companyId, Integer seatId) {
        Seat seat = seatRepository.findByIdAndCompanyId(seatId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế với ID: " + seatId));

        // Cannot delete BOOKED seats
        if (seat.getStatus() == SeatStatus.BOOKED) {
            throw new IllegalArgumentException("Không thể xóa ghế đã được đặt");
        }

        // Soft delete by changing status to DELETED
        seat.setStatus(SeatStatus.DELETED);
        seatRepository.save(seat);

        log.info("Soft deleted seat: {} for company: {}", seat.getSeatNumber(), companyId);
    }

    // ==================== HELPER METHODS ====================

    private Pageable createPageable(SeatSearchRequest request) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private SeatResponse convertToUserResponse(Seat seat) {
        return SeatResponse.forUser(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getSeatType(),
                seat.getStatus(),
                seat.getPriceForSeatType(),
                seat.getRowNumber(),
                seat.getColumnNumber());
    }

    private SeatResponse convertToCompanyResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setId(seat.getId());
        response.setSeatNumber(seat.getSeatNumber());
        response.setSeatType(seat.getSeatType());
        response.setStatus(seat.getStatus());
        response.setPriceForSeatType(seat.getPriceForSeatType());
        response.setRowNumber(seat.getRowNumber());
        response.setColumnNumber(seat.getColumnNumber());
        response.setBusId(seat.getBus().getId());
        response.setBusName(seat.getBus().getName());
        response.setCompanyName(seat.getBus().getCompany().getCompanyName());
        response.setCreatedAt(seat.getCreatedAt());
        response.setUpdatedAt(seat.getUpdatedAt());
        return response;
    }

    private void validateStatusTransition(SeatStatus currentStatus, SeatStatus newStatus) {
        // Business rules for status transitions
        if (currentStatus == SeatStatus.DELETED) {
            throw new IllegalArgumentException("Không thể thay đổi trạng thái ghế đã bị xóa");
        }

        if (currentStatus == SeatStatus.BOOKED && newStatus == SeatStatus.DELETED) {
            throw new IllegalArgumentException("Không thể xóa ghế đã được đặt");
        }

        // Add more business rules as needed
    }

    // ==================== STATISTICS METHODS ====================

    public long countSeatsByStatus(Integer companyId, SeatStatus status) {
        return seatRepository.countByCompanyIdAndStatus(companyId, status);
    }

    public List<Object[]> getSeatTypeStatistics(Integer companyId) {
        return seatRepository.countSeatTypesByCompanyId(companyId);
    }
}