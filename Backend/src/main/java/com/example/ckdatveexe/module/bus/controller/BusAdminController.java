package com.example.ckdatveexe.module.bus.controller;

import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.bus.dto.BusSearchRequest;
import com.example.ckdatveexe.module.bus.dto.BusUpdateRequest;
import com.example.ckdatveexe.module.bus.service.BusService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.BusStatus;
import com.example.ckdatveexe.shared.entity.BusType;
import com.example.ckdatveexe.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/buses")
@RequiredArgsConstructor
@Tag(name = "Bus Admin API", description = "API quản lý xe cho admin")
@SecurityRequirement(name = "Bearer Authentication") // Yêu cầu authentication cho toàn bộ controller
@Slf4j
public class BusAdminController {

        private final BusService busService;

        @GetMapping
        @Operation(summary = "Xem tất cả các xe (Admin)", description = "Lấy danh sách tất cả các xe với thông tin chi tiết")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getAllBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(required = false) BusType busType,
                        @RequestParam(required = false) BusStatus status,
                        @RequestParam(required = false) Integer companyId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("👑 [ADMIN] GET /api/admin/buses - Get all buses");
                log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusSearchRequest request = new BusSearchRequest();
                        request.setKeyword(keyword);
                        request.setBusType(busType);
                        request.setStatus(status);
                        request.setCompanyId(companyId);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<BusResponse> buses = busService.getAllBusesForAdmin(request);

                        log.info("✅ [ADMIN] 200 OK - Retrieved {} buses total", buses.getTotalElements());
                        return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách xe thành công")
                                        .data(buses)
                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get buses", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách xe")
                                                        .build());
                }
        }

        @GetMapping("/{busId}")
        @Operation(summary = "Xem chi tiết xe (Admin)", description = "Lấy thông tin chi tiết của một xe")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<BusResponse>> getBusDetail(@PathVariable Integer busId,
                        Authentication authentication) {

                log.info("👑 [ADMIN] GET /api/admin/buses/{} - Get bus detail", busId);
                log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusResponse bus = busService.getBusDetailForAdmin(busId);

                        log.info("✅ [ADMIN] 200 OK - Retrieved bus detail for ID: {}", busId);
                        return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                        .success(true)
                                        .message("Lấy thông tin xe thành công")
                                        .data(bus)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [ADMIN] 404 NOT_FOUND - Bus not found: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy xe với ID: " + busId)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid bus ID: {}", busId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("ID xe không hợp lệ: " + busId)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get bus detail for ID: {}", busId,
                                        e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy thông tin xe")
                                                        .build());
                }
        }

        @PutMapping("/{busId}")
        @Operation(summary = "Cập nhật xe (Admin)", description = "Cập nhật thông tin cơ bản của xe")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<BusResponse>> updateBus(
                        @PathVariable Integer busId,
                        @Valid @RequestBody BusUpdateRequest request,
                        Authentication authentication) {

                log.info("👑 [ADMIN] PUT /api/admin/buses/{} - Update bus", busId);
                log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusResponse updatedBus = busService.updateBusForAdmin(busId, request);

                        log.info("✅ [ADMIN] 200 OK - Bus updated successfully with ID: {}", busId);
                        return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                        .success(true)
                                        .message("Cập nhật xe thành công")
                                        .data(updatedBus)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [ADMIN] 404 NOT_FOUND - Bus not found: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy xe với ID: " + busId)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid update data for bus {}: {}", busId,
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Dữ liệu cập nhật không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to update bus with ID: {}", busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi cập nhật xe")
                                                        .build());
                }
        }

        @GetMapping("/company/{companyId}")
        @Operation(summary = "Xe của nhà xe (Admin)", description = "Lấy danh sách xe của một nhà xe cụ thể")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getBusesByCompany(
                        @PathVariable Integer companyId,
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(required = false) BusStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("👑 [ADMIN] GET /api/admin/buses/company/{} - Get buses by company", companyId);
                log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusSearchRequest request = new BusSearchRequest();
                        request.setKeyword(keyword);
                        request.setStatus(status);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<BusResponse> buses = busService.getBusesByCompanyForAdmin(companyId, request);

                        log.info("✅ [ADMIN] 200 OK - Retrieved {} buses for company ID: {}", buses.getTotalElements(),
                                        companyId);
                        return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách xe của nhà xe thành công")
                                        .data(buses)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [ADMIN] 404 NOT_FOUND - Company not found: {}", companyId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy nhà xe với ID: " + companyId)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid search parameters for company {}: {}", companyId,
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get buses for company: {}",
                                        companyId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách xe của nhà xe")
                                                        .build());
                }
        }
}