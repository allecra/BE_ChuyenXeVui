package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.module.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bus-company")
@RequiredArgsConstructor
@Tag(name = "Bus Company Admin API", description = "API quản lý nhà xe dành cho admin")
@SecurityRequirement(name = "Bearer Authentication")
public class BusCompanyAdminController {

        private final BusCompanyService busCompanyService;

        @GetMapping("/bus-owner")
        @Operation(summary = "Xe thuộc về nhà xe nào", description = "API để xem xe thuộc về nhà xe nào (dành cho admin)")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<BusCompanyResponse>> getBusCompanyByBusId(
                        @RequestParam Integer busId) {

                BusCompanyResponse company = busCompanyService.getBusCompanyByBusId(busId);

                return ResponseEntity.ok(ApiResponse.<BusCompanyResponse>builder()
                                .success(true)
                                .message("Lấy thông tin nhà xe sở hữu xe thành công")
                                .data(company)
                                .build());
        }

        @PostMapping("/reset-password")
        @Operation(summary = "Reset mật khẩu nhà xe", description = "Reset mật khẩu cho tài khoản nhà xe và gửi email thông báo")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<Void>> resetBusCompanyPassword(
                        @RequestParam String email) {

                busCompanyService.resetBusCompanyPassword(email);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Đã reset mật khẩu và gửi email thông báo thành công")
                                .build());
        }
}