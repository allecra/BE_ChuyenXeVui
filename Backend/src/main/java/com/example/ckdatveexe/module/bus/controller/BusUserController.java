package com.example.ckdatveexe.module.bus.controller;

import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.bus.dto.BusSearchRequest;
import com.example.ckdatveexe.module.bus.service.BusService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/buses")
@RequiredArgsConstructor
@Tag(name = "Bus User API", description = "API quản lý xe cho người dùng")
@SecurityRequirement(name = "Bearer Authentication") // Yêu cầu authentication cho toàn bộ controller
@Slf4j
public class BusUserController {

        private final BusService busService;

        @GetMapping
        @Operation(summary = "Xem tất cả các xe", description = "Lấy danh sách tất cả các xe khả dụng")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getAllBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("👤 [USER] GET /api/user/buses - Get all buses");
                log.info("🔐 [USER] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusSearchRequest request = new BusSearchRequest();
                        request.setKeyword(keyword);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<BusResponse> buses = busService.getAllBusesForUser(request);

                        log.info("✅ [USER] 200 OK - Retrieved {} buses for user", buses.getTotalElements());
                        return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách xe thành công")
                                        .data(buses)
                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [USER] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get buses", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách xe")
                                                        .build());
                }
        }

        @GetMapping("/{busId}")
        @Operation(summary = "Xem chi tiết xe", description = "Lấy thông tin chi tiết của một xe")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> getBusDetail(@PathVariable Integer busId,
                        Authentication authentication) {

                log.info("👤 [USER] GET /api/user/buses/{} - Get bus detail", busId);
                log.info("🔐 [USER] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusResponse bus = busService.getBusDetailForUser(busId);

                        log.info("✅ [USER] 200 OK - Retrieved bus detail for ID: {}", busId);
                        return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                        .success(true)
                                        .message("Lấy thông tin xe thành công")
                                        .data(bus)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [USER] 404 NOT_FOUND - Bus not found: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy xe với ID: " + busId)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [USER] 400 BAD_REQUEST - Invalid bus ID: {}", busId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("ID xe không hợp lệ: " + busId)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get bus detail for ID: {}", busId,
                                        e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy thông tin xe")
                                                        .build());
                }
        }

        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm xe", description = "Tìm kiếm xe theo từ khóa")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> searchBuses(
                        @RequestParam String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("👤 [USER] GET /api/user/buses/search - Search buses with keyword: {}", keyword);
                log.info("🔐 [USER] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        if (keyword == null || keyword.trim().isEmpty()) {
                                log.error("❌ [USER] 400 BAD_REQUEST - Empty search keyword");
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.<Page<BusResponse>>builder()
                                                                .success(false)
                                                                .message("Từ khóa tìm kiếm không được để trống")
                                                                .build());
                        }

                        BusSearchRequest request = new BusSearchRequest();
                        request.setKeyword(keyword);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<BusResponse> buses = busService.searchBusesForUser(request);

                        log.info("✅ [USER] 200 OK - Found {} buses matching keyword: {}", buses.getTotalElements(),
                                        keyword);
                        return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                        .success(true)
                                        .message("Tìm kiếm xe thành công")
                                        .data(buses)
                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [USER] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to search buses with keyword: {}",
                                        keyword, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tìm kiếm xe")
                                                        .build());
                }
        }

        @GetMapping("/company/{companyId}")
        @Operation(summary = "Xe của nhà xe", description = "Lấy danh sách xe của một nhà xe cụ thể")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getBusesByCompany(
                        @PathVariable Integer companyId,
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("👤 [USER] GET /api/user/buses/company/{} - Get buses by company", companyId);
                log.info("🔐 [USER] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusSearchRequest request = new BusSearchRequest();
                        request.setKeyword(keyword);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<BusResponse> buses = busService.getBusesByCompanyForUser(companyId, request);

                        log.info("✅ [USER] 200 OK - Retrieved {} buses for company ID: {}", buses.getTotalElements(),
                                        companyId);
                        return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách xe của nhà xe thành công")
                                        .data(buses)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [USER] 404 NOT_FOUND - Company not found: {}", companyId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy nhà xe với ID: " + companyId)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [USER] 400 BAD_REQUEST - Invalid search parameters for company {}: {}", companyId,
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get buses for company: {}",
                                        companyId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách xe của nhà xe")
                                                        .build());
                }
        }
}