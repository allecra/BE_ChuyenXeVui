package com.example.ckdatveexe.module.bus.service;

import com.example.ckdatveexe.module.bus.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import com.example.ckdatveexe.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;
    private final DeletedBusRepository deletedBusRepository;
    private final SeatRepository seatRepository;
    private final BusCompanyRepository busCompanyRepository;
    private final SeatGenerationService seatGenerationService;

    // ==================== USER APIs ====================

    public Page<BusResponse> getAllBusesForUser(BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Bus> buses;

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            buses = busRepository.searchBuses(
                    request.getKeyword().trim(),
                    BusStatus.ACTIVE,
                    pageable);
        } else {
            buses = busRepository.findByStatus(BusStatus.ACTIVE, pageable);
        }

        return buses.map(this::convertToBusResponseForUser);
    }

    public BusResponse getBusDetailForUser(Integer busId) {
        Bus bus = findBusById(busId);
        if (bus.getStatus() != BusStatus.ACTIVE) {
            throw new ResourceNotFoundException("Xe không khả dụng");
        }
        return convertToBusResponseForUser(bus);
    }

    public Page<BusResponse> searchBusesForUser(BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Bus> buses = busRepository.searchBuses(
                request.getKeyword(),
                BusStatus.ACTIVE,
                pageable);
        return buses.map(this::convertToBusResponseForUser);
    }

    public Page<BusResponse> getBusesByCompanyForUser(Integer companyId, BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Bus> buses;

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            buses = busRepository.searchBusesByCompany(
                    companyId,
                    request.getKeyword().trim(),
                    BusStatus.ACTIVE,
                    pageable);
        } else {
            buses = busRepository.findByCompanyIdAndStatus(companyId, BusStatus.ACTIVE, pageable);
        }

        return buses.map(this::convertToBusResponseForUser);
    }

    // ==================== ADMIN APIs ====================

    public Page<BusResponse> getAllBusesForAdmin(BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Bus> buses;

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            buses = busRepository.searchBuses(
                    request.getKeyword().trim(),
                    request.getStatus() != null ? request.getStatus() : BusStatus.ACTIVE,
                    pageable);
        } else {
            if (request.getStatus() != null) {
                buses = busRepository.findByStatus(request.getStatus(), pageable);
            } else {
                buses = busRepository.findAll(pageable);
            }
        }

        return buses.map(this::convertToBusResponseForAdmin);
    }

    public BusResponse getBusDetailForAdmin(Integer busId) {
        Bus bus = findBusById(busId);
        return convertToBusResponseForAdmin(bus);
    }

    @Transactional
    public BusResponse updateBusForAdmin(Integer busId, BusUpdateRequest request) {
        Bus bus = findBusById(busId);

        // Kiểm tra biển số trùng
        if (busRepository.existsByLicensePlateAndIdNot(request.getLicensePlate(), busId)) {
            throw new IllegalArgumentException("Biển số xe đã tồn tại");
        }

        bus.setName(request.getName());
        bus.setDescriptions(request.getDescriptions());
        bus.setLicensePlate(request.getLicensePlate());
        bus.setCapacity(request.getCapacity());
        bus.setBusType(request.getBusType());
        if (request.getStatus() != null) {
            bus.setStatus(request.getStatus());
        }

        Bus savedBus = busRepository.save(bus);
        return convertToBusResponseForAdmin(savedBus);
    }

    public Page<BusResponse> getBusesByCompanyForAdmin(Integer companyId, BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Bus> buses;

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            buses = busRepository.searchBusesByCompany(
                    companyId,
                    request.getKeyword().trim(),
                    request.getStatus() != null ? request.getStatus() : BusStatus.ACTIVE,
                    pageable);
        } else {
            if (request.getStatus() != null) {
                buses = busRepository.findByCompanyIdAndStatus(companyId, request.getStatus(), pageable);
            } else {
                buses = busRepository.findByCompanyId(companyId, pageable);
            }
        }

        return buses.map(this::convertToBusResponseForAdmin);
    }

    // ==================== BUS COMPANY APIs ====================

    @Transactional
    public BusResponse createBus(Integer companyId, BusCreateRequest request) {
        // Kiểm tra nhà xe tồn tại
        BusCompany company = busCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhà xe không tồn tại"));

        // Kiểm tra biển số trùng
        if (busRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new IllegalArgumentException("Biển số xe đã tồn tại");
        }

        Bus bus = new Bus();
        bus.setName(request.getName());
        bus.setDescriptions(request.getDescriptions());
        bus.setLicensePlate(request.getLicensePlate());
        bus.setCapacity(request.getCapacity());
        bus.setBusType(request.getBusType());
        bus.setCompany(company);
        bus.setStatus(BusStatus.ACTIVE);

        Bus savedBus = busRepository.save(bus);

        // Tự động sinh ghế theo loại xe
        seatGenerationService.generateSeatsForBus(savedBus);

        return convertToBusResponseForBusCompany(savedBus);
    }

    public Page<BusResponse> getBusesForCompany(Integer companyId, BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Bus> buses;

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            buses = busRepository.searchBusesByCompany(
                    companyId,
                    request.getKeyword().trim(),
                    request.getStatus() != null ? request.getStatus() : BusStatus.ACTIVE,
                    pageable);
        } else {
            if (request.getStatus() != null) {
                buses = busRepository.findByCompanyIdAndStatus(companyId, request.getStatus(), pageable);
            } else {
                buses = busRepository.findByCompanyId(companyId, pageable);
            }
        }

        return buses.map(this::convertToBusResponseForBusCompany);
    }

    public BusResponse getBusDetailForCompany(Integer companyId, Integer busId) {
        Bus bus = findBusById(busId);
        if (!bus.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }
        return convertToBusResponseForBusCompany(bus);
    }

    @Transactional
    public BusResponse updateBusForCompany(Integer companyId, Integer busId, BusUpdateRequest request) {
        Bus bus = findBusById(busId);
        if (!bus.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        // Kiểm tra biển số trùng
        if (busRepository.existsByLicensePlateAndIdNot(request.getLicensePlate(), busId)) {
            throw new IllegalArgumentException("Biển số xe đã tồn tại");
        }

        bus.setName(request.getName());
        bus.setDescriptions(request.getDescriptions());
        bus.setLicensePlate(request.getLicensePlate());
        bus.setCapacity(request.getCapacity());
        bus.setBusType(request.getBusType());
        if (request.getStatus() != null) {
            bus.setStatus(request.getStatus());
        }

        Bus savedBus = busRepository.save(bus);
        return convertToBusResponseForBusCompany(savedBus);
    }

    @Transactional
    public void deleteBus(Integer companyId, Integer busId, DeleteBusRequest request, Integer deletedBy) {
        Bus bus = findBusById(busId);
        if (!bus.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        if (request.isHardDelete()) {
            // Xóa cứng - xóa hoàn toàn khỏi database
            busRepository.delete(bus);
        } else {
            // Xóa mềm - chuyển vào bảng deleted_buses
            DeletedBus deletedBus = new DeletedBus();
            deletedBus.setOriginalBusId(bus.getId());
            deletedBus.setName(bus.getName());
            deletedBus.setDescriptions(bus.getDescriptions());
            deletedBus.setLicensePlate(bus.getLicensePlate());
            deletedBus.setCapacity(bus.getCapacity());
            deletedBus.setCompanyId(bus.getCompany().getId());
            deletedBus.setCompanyName(bus.getCompany().getCompanyName());
            deletedBus.setBusType(bus.getBusType());
            deletedBus.setOriginalCreatedAt(bus.getCreatedAt());
            deletedBus.setOriginalUpdatedAt(bus.getUpdatedAt());
            deletedBus.setDeletedBy(deletedBy);
            deletedBus.setDeletionReason(request.getDeletionReason());

            deletedBusRepository.save(deletedBus);
            busRepository.delete(bus);
        }
    }

    @Transactional
    public void updateSeatLayout(Integer companyId, Integer busId, SeatLayoutRequest request) {
        Bus bus = findBusById(busId);
        if (!bus.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        // Cập nhật layout ghế
        for (SeatLayoutRequest.SeatLayoutItem item : request.getSeats()) {
            if (item.getSeatId() != null) {
                Seat seat = seatRepository.findById(item.getSeatId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ghế không tồn tại"));

                if (!seat.getBus().getId().equals(busId)) {
                    throw new IllegalArgumentException("Ghế không thuộc về xe này");
                }

                seat.setSeatNumber(item.getSeatNumber());
                seat.setRowNumber(item.getRowNumber());
                seat.setColumnNumber(item.getColumnNumber());
                seat.setPriceForSeatType(item.getPriceForSeatType());

                seatRepository.save(seat);
            }
        }
    }

    @Transactional
    public void regenerateSeats(Integer companyId, Integer busId) {
        Bus bus = findBusById(busId);
        if (!bus.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        // Tái tạo ghế theo loại xe
        seatGenerationService.generateSeatsForBus(bus);
    }

    @Transactional
    public BusResponse restoreDeletedBus(Integer companyId, Integer deletedBusId) {
        DeletedBus deletedBus = deletedBusRepository.findById(deletedBusId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe đã xóa"));

        if (!deletedBus.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        // Tạo lại Bus từ DeletedBus
        Bus restoredBus = new Bus();
        restoredBus.setName(deletedBus.getName());
        restoredBus.setDescriptions(deletedBus.getDescriptions());
        restoredBus.setLicensePlate(deletedBus.getLicensePlate());
        restoredBus.setCapacity(deletedBus.getCapacity());
        restoredBus.setBusType(deletedBus.getBusType());
        restoredBus.setStatus(BusStatus.ACTIVE);

        // Lấy BusCompany
        BusCompany company = busCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe"));
        restoredBus.setCompany(company);

        Bus savedBus = busRepository.save(restoredBus);

        // Tự động sinh ghế theo loại xe
        seatGenerationService.generateSeatsForBus(savedBus);

        // Xóa khỏi bảng deleted_buses
        deletedBusRepository.delete(deletedBus);

        return convertToBusResponseForBusCompany(savedBus);
    }

    public Page<DeletedBus> getDeletedBuses(Integer companyId, BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        return deletedBusRepository.findByCompanyId(companyId, pageable);
    }

    // ==================== HELPER METHODS ====================

    private Bus findBusById(Integer busId) {
        return busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe không tồn tại"));
    }

    private Pageable createPageable(BusSearchRequest request) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private BusResponse convertToBusResponseForUser(Bus bus) {
        BusResponse response = new BusResponse();
        response.setId(bus.getId());
        response.setName(bus.getName());
        response.setDescriptions(bus.getDescriptions());
        response.setLicensePlate(bus.getLicensePlate());
        response.setCapacity(bus.getCapacity());
        response.setBusType(bus.getBusType());
        response.setStatus(bus.getStatus());
        response.setCompanyName(bus.getCompany().getCompanyName());

        // Thông tin ghế cơ bản
        List<Seat> seats = seatRepository.findByBusIdOrderByRowNumberAscColumnNumberAsc(bus.getId());
        response.setTotalSeats(seats.size());
        response.setAvailableSeats((int) seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count());

        return response;
    }

    private BusResponse convertToBusResponseForAdmin(Bus bus) {
        BusResponse response = convertToBusResponseForUser(bus);
        response.setCreatedAt(bus.getCreatedAt());
        response.setUpdatedAt(bus.getUpdatedAt());
        response.setCompanyId(bus.getCompany().getId());

        // Thông tin chi tiết ghế
        List<Seat> seats = seatRepository.findByBusIdOrderByRowNumberAscColumnNumberAsc(bus.getId());
        response.setSeats(seats.stream().map(this::convertToSeatResponse).collect(Collectors.toList()));

        return response;
    }

    private BusResponse convertToBusResponseForBusCompany(Bus bus) {
        return convertToBusResponseForAdmin(bus);
    }

    private SeatResponse convertToSeatResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setId(seat.getId());
        response.setSeatNumber(seat.getSeatNumber());
        response.setSeatType(seat.getSeatType());
        response.setStatus(seat.getStatus());
        response.setPriceForSeatType(seat.getPriceForSeatType());
        response.setRowNumber(seat.getRowNumber());
        response.setColumnNumber(seat.getColumnNumber());
        return response;
    }
}