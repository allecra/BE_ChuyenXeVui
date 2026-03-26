package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.module.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyUpdateRequest;
import com.example.ckdatveexe.module.buscompany.dto.ChangePasswordRequest;
import com.example.ckdatveexe.module.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company/management")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ROLE_BUS_COMPANY')")
@Tag(name = "Bus Company Management APIs", description = "APIs quản lý cho nhà xe (sau khi được duyệt)")
public class BusCompanyManagementController {

    private final BusCompanyService busCompanyService;

    @PutMapping("/{companyId}")
    @Operation(summary = "Cập nhật thông tin nhà xe")
    public ResponseEntity<ApiResponse<BusCompanyResponse>> updateBusCompany(
            @PathVariable Integer companyId,
            @Valid @RequestBody BusCompanyUpdateRequest request) {

        BusCompanyResponse response = busCompanyService.updateBusCompany(companyId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin nhà xe thành công", response));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        busCompanyService.changePassword(email, request);

        return ResponseEntity.ok(ApiResponse.<Void>success(
                "Đổi mật khẩu thành công. Email thông báo đã được gửi."));
    }

    @GetMapping("/{companyId}/buses")
    @Operation(summary = "Xem danh sách xe đang hoạt động của nhà xe")
    public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> getActiveBuses(
            @PathVariable Integer companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BusCompanyResponse> buses = busCompanyService.getActiveBuses(companyId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách xe thành công", buses));
    }

    @GetMapping("/{companyId}/profile")
    @Operation(summary = "Xem thông tin chi tiết nhà xe của mình")
    public ResponseEntity<ApiResponse<BusCompanyResponse>> getCompanyProfile(@PathVariable Integer companyId) {
        BusCompanyResponse company = busCompanyService.getBusCompanyDetail(companyId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhà xe thành công", company));
    }
}