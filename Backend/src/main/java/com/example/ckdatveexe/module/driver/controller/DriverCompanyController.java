package com.example.ckdatveexe.module.driver.controller;

import com.example.ckdatveexe.module.driver.dto.*;
import com.example.ckdatveexe.module.driver.service.DriverService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.DriverStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/company/drivers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Driver Company", description = "APIs quản lý tài xế cho nhà xe")
public class DriverCompanyController {

    private final DriverService driverService;

    @GetMapping
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy danh sách tài xế", description = "Lấy danh sách tài xế với phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<Page<DriverResponse>>> getDrivers(
            @Parameter(description = "Trạng thái tài xế") @RequestParam(required = false) DriverStatus status,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "fullName") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            log.info("🏢 [GET] /api/company/drivers - Status: {}, Keyword: {}, Page: {}", status, keyword, page);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DriverResponse> drivers = driverService.getDrivers(status, keyword, pageable);

            log.info("✅ [GET] /api/company/drivers - Success: {} drivers found", drivers.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách tài xế thành công",
                    drivers));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/drivers - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy thông tin chi tiết tài xế", description = "Lấy thông tin chi tiết của một tài xế")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriverById(
            @Parameter(description = "ID tài xế") @PathVariable Integer id) {

        try {
            log.info("🏢 [GET] /api/company/drivers/{} - Getting driver details", id);

            DriverResponse driver = driverService.getDriverById(id);

            log.info("✅ [GET] /api/company/drivers/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin tài xế thành công",
                    driver));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/drivers/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Tạo tài xế mới", description = "Tạo tài xế mới trong hệ thống")
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(
            @Valid @RequestBody DriverCreateRequest request) {

        try {
            log.info("🏢 [POST] /api/company/drivers - Creating driver: {}", request.getFullName());

            DriverResponse driver = driverService.createDriver(request);

            log.info("✅ [POST] /api/company/drivers - Success: Driver created with ID {}", driver.getId());
            return ResponseEntity.ok(ApiResponse.success(
                    "Tạo tài xế thành công",
                    driver));
        } catch (Exception e) {
            log.error("💥 [POST] /api/company/drivers - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Cập nhật thông tin tài xế", description = "Cập nhật thông tin tài xế")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @Parameter(description = "ID tài xế") @PathVariable Integer id,
            @Valid @RequestBody DriverUpdateRequest request) {

        try {
            log.info("🏢 [PUT] /api/company/drivers/{} - Updating driver", id);

            DriverResponse driver = driverService.updateDriver(id, request);

            log.info("✅ [PUT] /api/company/drivers/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Cập nhật tài xế thành công",
                    driver));
        } catch (Exception e) {
            log.error("💥 [PUT] /api/company/drivers/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Xóa tài xế", description = "Xóa tài xế khỏi hệ thống")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(
            @Parameter(description = "ID tài xế") @PathVariable Integer id) {
        
        try {
            log.info("🏢 [DELETE] /api/company/drivers/{} - Deleting driver", id);

            driverService.deleteDriver(id);

            log.info("✅ [DELETE] /api/company/drivers/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success("Xóa tài xế thành công", null));
        } catch (Exception e) {
            log.error("💥 [DELETE] /api/company/drivers/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy danh sách tài xế có sẵn", description = "Lấy danh sách tài xế đang hoạt động và chưa được phân công xe")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAvailableDrivers() {
        
        try {
            log.info("🏢 [GET] /api/company/drivers/available - Getting available drivers");

            List<DriverResponse> drivers = driverService.getAvailableDrivers();

            log.info("✅ [GET] /api/company/drivers/available - Success: {} available drivers", drivers.size());
            return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách tài xế có sẵn thành công", 
                drivers));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/drivers/available - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/assign-bus/{busId}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Phân công xe cho tài xế", description = "Phân công xe cho tài xế")
    public ResponseEntity<ApiResponse<DriverResponse>> assignBusToDriver(
            @Parameter(description = "ID tài xế") @PathVariable Integer id,
            @Parameter(description = "ID xe") @PathVariable Integer busId) {
        
        try {
            log.info("🏢 [POST] /api/company/drivers/{}/assign-bus/{} - Assigning bus to driver", id, busId);

            DriverResponse driver = driverService.assignBusToDriver(id, busId);

            log.info("✅ [POST] /api/company/drivers/{}/assign-bus/{} - Success", id, busId);
            return ResponseEntity.ok(ApiResponse.success(
                "Phân công xe cho tài xế thành công", 
                driver));
        } catch (Exception e) {
            log.error("💥 [POST] /api/company/drivers/{}/assign-bus/{} - Error: {}", id, busId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/unassign-bus")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Hủy phân công xe cho tài xế", description = "Hủy phân công xe cho tài xế")
    public ResponseEntity<ApiResponse<DriverResponse>> unassignBusFromDriver(
            @Parameter(description = "ID tài xế") @PathVariable Integer id) {
        
        try {
            log.info("🏢 [POST] /api/company/drivers/{}/unassign-bus - Unassigning bus from driver", id);

            DriverResponse driver = driverService.unassignBusFromDriver(id);

            log.info("✅ [POST] /api/company/drivers/{}/unassign-bus - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                "Hủy phân công xe cho tài xế thành công", 
                driver));
        } catch (Exception e) {
            log.error("💥 [POST] /api/company/drivers/{}/unassign-bus - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Cập nhật trạng thái tài xế", description = "Cập nhật trạng thái hoạt động của tài xế")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriverStatus(
            @Parameter(description = "ID tài xế") @PathVariable Integer id,
            @Parameter(description = "Trạng thái mới") @RequestParam DriverStatus status) {
        
        try {
            log.info("🏢 [PUT] /api/company/drivers/{}/status - Updating status to {}", id, status);

            DriverResponse driver = driverService.updateDriverStatus(id, status);

            log.info("✅ [PUT] /api/company/drivers/{}/status - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái tài xế thành công", 
                driver));
        } catch (Exception e) {
            log.error("💥 [PUT] /api/company/drivers/{}/status - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/expiring-licenses")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Lấy danh sách tài xế có bằng lái sắp hết hạn", description = "Lấy danh sách tài xế có bằng lái sắp hết hạn trong 30 ngày tới")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getDriversWithExpiringLicenses() {
        
        try {
            log.info("🏢 [GET] /api/company/drivers/expiring-licenses - Getting drivers with expiring licenses");

            List<DriverResponse> drivers = driverService.getDriversWithExpiringLicenses();

            log.info("✅ [GET] /api/company/drivers/expiring-licenses - Success: {} drivers found", drivers.size());
            return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách tài xế có bằng lái sắp hết hạn thành công", 
                drivers));
        } catch (Exception e) {
            log.error("💥 [GET] /api/company/drivers/expiring-licenses - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}