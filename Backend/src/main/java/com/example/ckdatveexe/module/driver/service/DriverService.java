package com.example.ckdatveexe.module.driver.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.driver.dto.*;
import com.example.ckdatveexe.shared.entity.Bus;
import com.example.ckdatveexe.shared.entity.Driver;
import com.example.ckdatveexe.shared.entity.DriverStatus;
import com.example.ckdatveexe.shared.repository.BusRepository;
import com.example.ckdatveexe.shared.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;
    private final BusRepository busRepository;

    // ===== USER METHODS (READ-ONLY) =====

    public Page<DriverResponse> getDrivers(DriverStatus status, String keyword, Pageable pageable) {
        log.info("🚗 Getting drivers list - Status: {}, Keyword: {}, Page: {}", status, keyword,
                pageable.getPageNumber());

        Page<Driver> drivers;

        if (keyword != null && !keyword.trim().isEmpty()) {
            if (status != null) {
                drivers = driverRepository.searchByKeywordAndStatus(keyword.trim(), status, pageable);
            } else {
                drivers = driverRepository.searchByKeyword(keyword.trim(), pageable);
            }
        } else {
            if (status != null) {
                drivers = driverRepository.findByStatus(status, pageable);
            } else {
                drivers = driverRepository.findAll(pageable);
            }
        }

        log.info("✅ Retrieved {} drivers", drivers.getTotalElements());
        return drivers.map(DriverResponse::fromEntity);
    }

    public DriverResponse getDriverById(Integer id) {
        log.info("🚗 Getting driver by ID: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế không tồn tại"));

        log.info("✅ Driver retrieved successfully: {}", driver.getFullName());
        return DriverResponse.fromEntity(driver);
    }

    public List<DriverResponse> getAvailableDrivers() {
        log.info("🚗 Getting available drivers");

        List<Driver> drivers = driverRepository.findAvailableDrivers();

        log.info("✅ Found {} available drivers", drivers.size());
        return drivers.stream()
                .map(DriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ===== COMPANY METHODS (FULL CRUD) =====

    @Transactional
    public DriverResponse createDriver(DriverCreateRequest request) {
        log.info("🚗 Creating new driver: {}", request.getFullName());

        // Validate unique constraints
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("Số bằng lái đã tồn tại trong hệ thống");
        }

        if (driverRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi tài xế khác");
        }

        // Create driver entity
        Driver driver = new Driver();
        driver.setFullName(request.getFullName());
        driver.setPhone(request.getPhone());
        driver.setEmail(request.getEmail());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseType(request.getLicenseType());
        driver.setLicenseExpiryDate(request.getLicenseExpiryDate());
        driver.setDateOfBirth(request.getDateOfBirth());
        driver.setAddress(request.getAddress());
        driver.setStatus(request.getStatus() != null ? request.getStatus() : DriverStatus.ACTIVE);
        driver.setTotalTrips(0);
        driver.setNotes(request.getNotes());

        // Set current bus if provided
        if (request.getCurrentBusId() != null) {
            Bus bus = busRepository.findById(request.getCurrentBusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Xe không tồn tại"));

            // Check if bus already has a driver
            driverRepository.findByCurrentBusId(request.getCurrentBusId())
                    .ifPresent(existingDriver -> {
                        throw new IllegalArgumentException("Xe đã có tài xế được phân công");
                    });

            driver.setCurrentBus(bus);
        }

        driver = driverRepository.save(driver);

        log.info("✅ Driver created successfully: {} (ID: {})", driver.getFullName(), driver.getId());
        return DriverResponse.fromEntity(driver);
    }

    @Transactional
    public DriverResponse updateDriver(Integer id, DriverUpdateRequest request) {
        log.info("📝 Updating driver: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế không tồn tại"));

        // Validate unique constraints
        if (!driver.getLicenseNumber().equals(request.getLicenseNumber())) {
            if (driverRepository.existsByLicenseNumberAndIdNot(request.getLicenseNumber(), id)) {
                throw new IllegalArgumentException("Số bằng lái đã tồn tại trong hệ thống");
            }
        }

        if (!driver.getPhone().equals(request.getPhone())) {
            if (driverRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi tài xế khác");
            }
        }

        // Update driver information
        driver.setFullName(request.getFullName());
        driver.setPhone(request.getPhone());
        driver.setEmail(request.getEmail());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseType(request.getLicenseType());
        driver.setLicenseExpiryDate(request.getLicenseExpiryDate());
        driver.setDateOfBirth(request.getDateOfBirth());
        driver.setAddress(request.getAddress());
        
        if (request.getStatus() != null) {
            driver.setStatus(request.getStatus());
        }
        
        driver.setNotes(request.getNotes());

        // Update current bus assignment
        if (request.getCurrentBusId() != null) {
            if (driver.getCurrentBus() == null || !driver.getCurrentBus().getId().equals(request.getCurrentBusId())) {
                Bus bus = busRepository.findById(request.getCurrentBusId())
                        .orElseThrow(() -> new ResourceNotFoundException("Xe không tồn tại"));
                
                // Check if bus already has another driver
                driverRepository.findByCurrentBusId(request.getCurrentBusId())
                        .ifPresent(existingDriver -> {
                            if (!existingDriver.getId().equals(id)) {
                                throw new IllegalArgumentException("Xe đã có tài xế được phân công");
                            }
                        });
                
                driver.setCurrentBus(bus);
            }
        } else {
            driver.setCurrentBus(null);
        }

        driver = driverRepository.save(driver);

        log.info("✅ Driver updated successfully: {}", driver.getFullName());
        return DriverResponse.fromEntity(driver);
    }

    @Transactional
    public void deleteDriver(Integer id) {
        log.info("🗑️ Deleting driver: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế không tồn tại"));

        // Check if driver is currently assigned to a bus
        if (driver.getCurrentBus() != null) {
            throw new IllegalArgumentException("Không thể xóa tài xế đang được phân công xe");
        }
      // Check if driver has active trips (this would require trip/schedule checking)
        // For now, we'll just check if status is ACTIVE
        if (driver.getStatus() == DriverStatus.ACTIVE) {
            throw new IllegalArgumentException("Không thể xóa tài xế đang hoạt động. Vui lòng chuyển sang trạng thái INACTIVE trước");
        }

        driverRepository.delete(driver);

        log.info("✅ Driver deleted successfully: {}", driver.getFullName());
    }

    @Transactional
    public DriverResponse assignBusToDriver(Integer driverId, Integer busId) {
        log.info("🚌 Assigning bus {} to driver {}", busId, driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế không tồn tại"));

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe không tồn tại"));

        // Check if bus already has a driver
        driverRepository.findByCurrentBusId(busId)
                .ifPresent(existingDriver -> {
                    if (!existingDriver.getId().equals(driverId)) {
                        throw new IllegalArgumentException("Xe đã có tài xế được phân công");
                    }
                });

        // Check if driver is available
        if (driver.getStatus() != DriverStatus.ACTIVE) {
            throw new IllegalArgumentException("Tài xế không ở trạng thái hoạt động");
        }

        driver.setCurrentBus(bus);
        driver = driverRepository.save(driver);

        log.info("✅ Bus assigned successfully to driver: {}", driver.getFullName());
        return DriverResponse.fromEntity(driver);
    }

    @Transactional
    public DriverResponse unassignBusFromDriver(Integer driverId) {
        log.info("🚌 Unassigning bus from driver {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế không tồn tại"));

        if (driver.getCurrentBus() == null) {
            throw new IllegalArgumentException("Tài xế chưa được phân công xe");
        }

        driver.setCurrentBus(null);
        driver = driverRepository.save(driver);

        log.info("✅ Bus unassigned successfully from driver: {}", driver.getFullName());
        return DriverResponse.fromEntity(driver);
    }

    @Transactional
    public DriverResponse updateDriverStatus(Integer id, DriverStatus status) {
        log.info("📝 Updating driver status: {} to {}", id, status);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế không tồn tại"));

        driver.setStatus(status);
        driver = driverRepository.save(driver);

        log.info("✅ Driver status updated successfully: {}", driver.getFullName());
        return DriverResponse.fromEntity(driver);
    }

    public List<DriverResponse> getDriversWithExpiringLicenses() {
        log.info("⚠️ Getting drivers with expiring licenses");

        List<Driver> drivers = driverRepository.findDriversWithExpiringLicenses();

        log.info("✅ Found {} drivers with expiring licenses", drivers.size());
        return drivers.stream()
                .map(DriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public long getDriverCountByStatus(DriverStatus status) {
        return driverRepository.countByStatus(status);
    }
}