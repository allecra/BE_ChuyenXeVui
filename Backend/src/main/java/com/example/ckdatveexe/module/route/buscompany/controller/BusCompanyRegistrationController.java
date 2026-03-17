package com.example.ckdatveexe.module.route.buscompany.controller;

import com.example.ckdatveexe.module.route.buscompany.dto.BusCompanyRegistrationRequest;
import com.example.ckdatveexe.module.route.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/bus-company")
@RequiredArgsConstructor
@Tag(name = "Bus Company Registration API", description = "API đăng ký nhà xe công khai (không yêu cầu đăng nhập)")
@SecurityRequirements() // Chỉ định không cần security cho toàn bộ controller
public class BusCompanyRegistrationController {

    private final BusCompanyService busCompanyService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký nhà xe", description = "Đăng ký nhà xe mới (không yêu cầu đăng nhập, chờ admin xác thực để tạo tài khoản)")
    @RequestBody(description = "Thông tin đăng ký nhà xe", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Đăng ký đầy đủ", description = "Ví dụ đăng ký nhà xe với đầy đủ thông tin", value = """
                    {
                        "companyName": "Nhà xe Phương Trang",
                        "email": "contact@phuongtrang.vn",
                        "phoneNumber": "0283 8386 852",
                        "image": "https://example.com/logo-phuong-trang.jpg",
                        "descriptions": "Nhà xe Phương Trang - Chuyên tuyến liên tỉnh với hơn 20 năm kinh nghiệm",
                        "businessLicense": "0123456789",
                        "address": "272 Đề Thám, Phường Phạm Ngũ Lão, Quận 1, TP.HCM"
                    }
                    """),
            @ExampleObject(name = "Đăng ký tối thiểu", description = "Đăng ký với thông tin tối thiểu bắt buộc", value = """
                    {
                        "companyName": "Nhà xe Mai Linh",
                        "email": "info@mailinh.vn",
                        "phoneNumber": "0283 8383 838"
                    }
                    """)
    }))
    public ResponseEntity<ApiResponse<Void>> registerBusCompany(
            @Valid @org.springframework.web.bind.annotation.RequestBody BusCompanyRegistrationRequest request) {

        busCompanyService.registerBusCompany(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>builder()
                        .success(true)
                        .message(
                                "Đăng ký nhà xe thành công! Chúng tôi sẽ xem xét và thông báo kết quả qua email trong thời gian sớm nhất.")
                        .build());
    }

    @GetMapping("/registration-status/{email}")
    @Operation(summary = "Kiểm tra trạng thái đăng ký", description = "Kiểm tra trạng thái đăng ký nhà xe bằng email")
    public ResponseEntity<ApiResponse<String>> checkRegistrationStatus(@PathVariable String email) {
        try {
            String status = busCompanyService.getRegistrationStatusByEmail(email);
            String message = switch (status) {
                case "PENDING" -> "Đơn đăng ký đang được xem xét";
                case "APPROVED" -> "Đơn đăng ký đã được duyệt. Vui lòng kiểm tra email để nhận thông tin đăng nhập";
                case "REJECTED" -> "Đơn đăng ký đã bị từ chối. Vui lòng kiểm tra email để biết lý do chi tiết";
                default -> "Không tìm thấy đơn đăng ký với email này";
            };

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message(message)
                    .data(status)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(false)
                    .message("Không tìm thấy đơn đăng ký với email này")
                    .build());
        }
    }
}