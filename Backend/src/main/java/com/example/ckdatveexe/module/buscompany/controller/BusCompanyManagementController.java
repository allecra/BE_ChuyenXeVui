package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.bus.dto.BusSearchRequest;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.buscompany.dto.ChangePasswordRequest;
import com.example.ckdatveexe.module.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.BusStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bus-company/management")
@RequiredArgsConstructor
@Tag(name = "Bus Company Management API", description = "API quản lý thông tin nhà xe")
@SecurityRequirement(name = "Bearer Authentication")
public class BusCompanyManagementController {

        private final BusCompanyService busCompanyService;

        @GetMapping("/my-company")
        @Operation(summary = "Thông tin nhà xe của tôi", description = "Lấy thông tin nhà xe mà user hiện tại thuộc về")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusCompanyResponse>> getMyCompany(
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer userId = userDetails.getUser().getId();

                BusCompanyResponse company = busCompanyService.getBusCompanyByUserId(userId);

                return ResponseEntity.ok(ApiResponse.<BusCompanyResponse>builder()
                                .success(true)
                                .message("Lấy thông tin nhà xe thành công")
                                .data(company)
                                .build());
        }

        @GetMapping("/company-buses")
        @Operation(summary = "Xe thuộc về nhà xe của tôi", description = "Xem danh sách xe thuộc về nhà xe hiện tại (dành cho bus company)")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getMyCompanyBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(required = false) BusStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

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

                return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                .success(true)
                                .message("Lấy danh sách xe của nhà xe thành công")
                                .data(buses)
                                .build());
        }

        @GetMapping("/company-buses/{busId}")
        @Operation(summary = "Chi tiết xe của nhà xe", description = "Xem chi tiết xe thuộc về nhà xe hiện tại (bao gồm thông tin ghế)")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> getMyCompanyBusDetail(
                        @PathVariable Integer busId,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();

                BusResponse bus = busCompanyService.getMyCompanyBusDetail(companyId, busId);

                return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                .success(true)
                                .message("Lấy chi tiết xe thành công")
                                .data(bus)
                                .build());
        }

        @PostMapping("/change-password")
        @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho tài khoản nhà xe và gửi email thông báo")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Void>> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer userId = userDetails.getUser().getId();

                busCompanyService.changeBusCompanyPassword(userId, request);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Đổi mật khẩu thành công và đã gửi email thông báo")
                                .build());
        }
}