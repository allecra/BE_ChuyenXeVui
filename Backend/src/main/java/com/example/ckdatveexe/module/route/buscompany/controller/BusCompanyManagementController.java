package com.example.ckdatveexe.module.route.buscompany.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.bus.dto.BusSearchRequest;
import com.example.ckdatveexe.module.route.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.route.buscompany.dto.BusCompanyUpdateRequest;
import com.example.ckdatveexe.module.route.buscompany.dto.ChangePasswordRequest;
import com.example.ckdatveexe.module.route.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.BusStatus;
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
@RequestMapping("/api/bus-company/management")
@RequiredArgsConstructor
@Tag(name = "Bus Company Management API", description = "API quản lý thông tin nhà xe")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class BusCompanyManagementController {

        private final BusCompanyService busCompanyService;

        @GetMapping("/my-company")
        @Operation(summary = "Thông tin nhà xe của tôi", description = "Lấy thông tin nhà xe mà user hiện tại thuộc về")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusCompanyResponse>> getMyCompany(
                        Authentication authentication) {

                log.info("🏢 [BUS COMPANY MGMT] GET /api/bus-company/management/my-company - Get my company info");
                log.info("🔐 [BUS COMPANY MGMT] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer userId = userDetails.getUser().getId();

                        BusCompanyResponse company = busCompanyService.getBusCompanyByUserId(userId);

                        log.info("✅ [BUS COMPANY MGMT] 200 OK - Retrieved company info for user ID: {}", userId);
                        return ResponseEntity.ok(ApiResponse.<BusCompanyResponse>builder()
                                        .success(true)
                                        .message("Lấy thông tin nhà xe thành công")
                                        .data(company)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY MGMT] 404 NOT_FOUND - Company not found for user");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy thông tin nhà xe")
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY MGMT] 500 INTERNAL_SERVER_ERROR - Failed to get company info", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy thông tin nhà xe")
                                                        .build());
                }
        }

        @GetMapping("/company-buses")
        @Operation(summary = "Xe thuộc về nhà xe của tôi", description = "Xem danh sách xe thuộc về nhà xe hiện tại (dành cho bus company)")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getMyCompanyBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(required = false) BusStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("🏢 [BUS COMPANY MGMT] GET /api/bus-company/management/company-buses - Get my company buses");
                log.info("🔐 [BUS COMPANY MGMT] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        BusSearchRequest request = new BusSearchRequest();
                        request.setKeyword(keyword);
                        request.setStatus(status);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<BusResponse> buses = busCompanyService.getMyCompanyBuses(companyId, request);

                        log.info("✅ [BUS COMPANY MGMT] 200 OK - Retrieved {} buses for company ID: {}",
                                        buses.getTotalElements(), companyId);
                        return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách xe của nhà xe thành công")
                                        .data(buses)
                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY MGMT] 400 BAD_REQUEST - Invalid search parameters: {}",
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY MGMT] 500 INTERNAL_SERVER_ERROR - Failed to get company buses", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách xe")
                                                        .build());
                }
        }

        @GetMapping("/company-buses/{busId}")
        @Operation(summary = "Chi tiết xe của nhà xe", description = "Xem chi tiết xe thuộc về nhà xe hiện tại (bao gồm thông tin ghế)")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> getMyCompanyBusDetail(
                        @PathVariable Integer busId,
                        Authentication authentication) {

                log.info("🏢 [BUS COMPANY MGMT] GET /api/bus-company/management/company-buses/{} - Get bus detail",
                                busId);
                log.info("🔐 [BUS COMPANY MGMT] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        BusResponse bus = busCompanyService.getMyCompanyBusDetail(companyId, busId);

                        log.info("✅ [BUS COMPANY MGMT] 200 OK - Retrieved bus detail for ID: {}", busId);
                        return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                        .success(true)
                                        .message("Lấy chi tiết xe thành công")
                                        .data(bus)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY MGMT] 404 NOT_FOUND - Bus not found: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Xe không tồn tại hoặc không thuộc về nhà xe này")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY MGMT] 400 BAD_REQUEST - Invalid bus ID: {}", busId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("ID xe không hợp lệ")
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY MGMT] 500 INTERNAL_SERVER_ERROR - Failed to get bus detail: {}",
                                        busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy chi tiết xe")
                                                        .build());
                }
        }

        @PostMapping("/change-password")
        @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho tài khoản nhà xe và gửi email thông báo")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Void>> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request,
                        Authentication authentication) {

                log.info("🏢 [BUS COMPANY MGMT] POST /api/bus-company/management/change-password - Change password");
                log.info("🔐 [BUS COMPANY MGMT] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer userId = userDetails.getUser().getId();

                        busCompanyService.changeBusCompanyPassword(userId, request);

                        log.info("✅ [BUS COMPANY MGMT] 200 OK - Password changed successfully for user ID: {}", userId);
                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message("Đổi mật khẩu thành công và đã gửi email thông báo")
                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY MGMT] 400 BAD_REQUEST - Invalid password data: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Dữ liệu mật khẩu không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY MGMT] 500 INTERNAL_SERVER_ERROR - Failed to change password", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi đổi mật khẩu")
                                                        .build());
                }
        }

        @PutMapping("/update-company")
        @Operation(summary = "Cập nhật thông tin nhà xe của tôi", description = "Cập nhật thông tin nhà xe hiện tại")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusCompanyResponse>> updateMyCompany(
                        @Valid @RequestBody BusCompanyUpdateRequest request,
                        Authentication authentication) {

                log.info("🏢 [BUS COMPANY MGMT] PUT /api/bus-company/management/update-company - Update my company");
                log.info("🔐 [BUS COMPANY MGMT] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        BusCompanyResponse updatedCompany = busCompanyService.updateMyCompany(companyId, request);

                        log.info("✅ [BUS COMPANY MGMT] 200 OK - Company updated successfully with ID: {}", companyId);
                        return ResponseEntity.ok(ApiResponse.<BusCompanyResponse>builder()
                                        .success(true)
                                        .message("Cập nhật thông tin nhà xe thành công")
                                        .data(updatedCompany)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY MGMT] 404 NOT_FOUND - Company not found for update");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Nhà xe không tồn tại")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY MGMT] 400 BAD_REQUEST - Invalid update data: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Dữ liệu cập nhật không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY MGMT] 500 INTERNAL_SERVER_ERROR - Failed to update company", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi cập nhật thông tin nhà xe")
                                                        .build());
                }
        }

        @GetMapping("/search-buses")
        @Operation(summary = "Tìm kiếm xe của nhà xe tôi", description = "Tìm kiếm xe thuộc về nhà xe hiện tại theo từ khóa")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> searchMyCompanyBuses(
                        @RequestParam String keyword,
                        @RequestParam(required = false) BusStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("🏢 [BUS COMPANY MGMT] GET /api/bus-company/management/search-buses - Search my company buses with keyword: {}",
                                keyword);
                log.info("🔐 [BUS COMPANY MGMT] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        if (keyword == null || keyword.trim().isEmpty()) {
                                log.error("❌ [BUS COMPANY MGMT] 400 BAD_REQUEST - Empty search keyword");
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.<Page<BusResponse>>builder()
                                                                .success(false)
                                                                .message("Từ khóa tìm kiếm không được để trống")
                                                                .build());
                        }

                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        BusSearchRequest request = new BusSearchRequest();
                        request.setKeyword(keyword);
                        request.setStatus(status);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<BusResponse> buses = busCompanyService.searchMyCompanyBuses(companyId, request);

                        log.info("✅ [BUS COMPANY MGMT] 200 OK - Found {} buses matching keyword: {}",
                                        buses.getTotalElements(), keyword);
                        return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                        .success(true)
                                        .message("Tìm kiếm xe thành công")
                                        .data(buses)
                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY MGMT] 400 BAD_REQUEST - Invalid search parameters: {}",
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY MGMT] 500 INTERNAL_SERVER_ERROR - Failed to search buses with keyword: {}",
                                        keyword, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tìm kiếm xe")
                                                        .build());
                }
        }
}