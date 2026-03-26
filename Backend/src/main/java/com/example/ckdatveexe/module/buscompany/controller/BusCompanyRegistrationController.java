package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.module.buscompany.dto.BusCompanyRegistrationRequest;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyRegistrationResponse;
import com.example.ckdatveexe.module.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bus-company/registration")
@RequiredArgsConstructor
@Tag(name = "Bus Company Registration APIs", description = "APIs cho đăng ký nhà xe (không cần auth)")
public class BusCompanyRegistrationController {

    private final BusCompanyService busCompanyService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký nhà xe mới")
    public ResponseEntity<ApiResponse<BusCompanyRegistrationResponse>> registerBusCompany(
            @Valid @RequestBody BusCompanyRegistrationRequest request) {

        BusCompanyRegistrationResponse response = busCompanyService.registerBusCompany(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Đăng ký nhà xe thành công. Vui lòng chờ admin xét duyệt. Email xác nhận đã được gửi.", response));
    }

    @GetMapping("/status")
    @Operation(summary = "Kiểm tra trạng thái đăng ký nhà xe")
    public ResponseEntity<ApiResponse<BusCompanyRegistrationResponse>> getRegistrationStatus(
            @RequestParam String email) {

        BusCompanyRegistrationResponse response = busCompanyService.getRegistrationStatus(email);
        return ResponseEntity.ok(ApiResponse.success("Lấy trạng thái đăng ký thành công", response));
    }
}