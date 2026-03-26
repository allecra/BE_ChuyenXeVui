package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.buscompany.dto.ApprovalRequest;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyRegistrationResponse;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyResponse;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bus-companies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Bus Company Admin APIs", description = "APIs quản lý nhà xe cho admin")
public class BusCompanyAdminController {

    private final BusCompanyService busCompanyService;

    @GetMapping("/registrations")
    @Operation(summary = "Lấy danh sách đơn đăng ký nhà xe")
    public ResponseEntity<ApiResponse<Page<BusCompanyRegistrationResponse>>> getAllRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BusCompanyRegistrationResponse> registrations = busCompanyService.getAllRegistrations(pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn đăng ký thành công", registrations));
    }

    @PostMapping("/registrations/{registrationId}/approve")
    @Operation(summary = "Duyệt hoặc từ chối đơn đăng ký nhà xe")
    public ResponseEntity<ApiResponse<BusCompanyRegistrationResponse>> approveRegistration(
            @PathVariable Integer registrationId,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Integer adminId = userDetails.getId();

        BusCompanyRegistrationResponse response = busCompanyService.approveRegistration(
                registrationId, request, adminId);

        String message = request.getStatus().name().equals("APPROVED")
                ? "Duyệt đơn đăng ký thành công. Email thông báo đã được gửi."
                : "Từ chối đơn đăng ký thành công. Email thông báo đã được gửi.";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả nhà xe")
    public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> getAllBusCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BusCompanyResponse> companies = busCompanyService.getAllBusCompanies(pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà xe thành công", companies));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm nhà xe")
    public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> searchBusCompanies(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BusCompanyResponse> companies = busCompanyService.searchBusCompanies(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm nhà xe thành công", companies));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết nhà xe")
    public ResponseEntity<ApiResponse<BusCompanyResponse>> getBusCompanyDetail(@PathVariable Integer id) {
        BusCompanyResponse company = busCompanyService.getBusCompanyDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết nhà xe thành công", company));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa nhà xe")
    public ResponseEntity<ApiResponse<Void>> deleteBusCompany(@PathVariable Integer id) {
        busCompanyService.deleteBusCompany(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Xóa nhà xe thành công"));
    }
}