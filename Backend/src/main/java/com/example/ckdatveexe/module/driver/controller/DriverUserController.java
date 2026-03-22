package com.example.ckdatveexe.module.driver.controller;

import com.example.ckdatveexe.module.driver.dto.DriverResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/user/drivers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Driver User", description = "APIs xem thông tin tài xế cho người dùng")
public class DriverUserController {

    private final DriverService driverService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy danh sách tài xế", description = "Lấy danh sách tài xế với phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<Page<DriverResponse>>> getDrivers(
            @Parameter(description = "Trạng thái tài xế") @RequestParam(required = false) DriverStatus status,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "fullName") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            log.info("👤 [GET] /api/user/drivers - Status: {}, Keyword: {}, Page: {}", status, keyword, page);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DriverResponse> drivers = driverService.getDrivers(status, keyword, pageable);

            log.info("✅ [GET] /api/user/drivers - Success: {} drivers found", drivers.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách tài xế thành công",
                    drivers));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/drivers - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy thông tin chi tiết tài xế", description = "Lấy thông tin chi tiết của một tài xế")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriverById(
            @Parameter(description = "ID tài xế") @PathVariable Integer id) {

        try {
            log.info("👤 [GET] /api/user/drivers/{} - Getting driver details", id);

            DriverResponse driver = driverService.getDriverById(id);

            log.info("✅ [GET] /api/user/drivers/{} - Success", id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin tài xế thành công",
                    driver));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/drivers/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy danh sách tài xế có sẵn", description = "Lấy danh sách tài xế đang hoạt động và chưa được phân công xe")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAvailableDrivers() {

        try {
            log.info("👤 [GET] /api/user/drivers/available - Getting available drivers");

            List<DriverResponse> drivers = driverService.getAvailableDrivers();

            log.info("✅ [GET] /api/user/drivers/available - Success: {} available drivers", drivers.size());
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách tài xế có sẵn thành công",
                    drivers));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/drivers/available - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}