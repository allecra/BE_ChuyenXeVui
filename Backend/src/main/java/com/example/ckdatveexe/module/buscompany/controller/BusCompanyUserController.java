package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.module.buscompany.dto.BusCompanyRegistrationRequest;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user/bus-companies")
@RequiredArgsConstructor
@Tag(name = "Bus Company User", description = "API quản lý nhà xe cho người dùng")
public class BusCompanyUserController {

        private final BusCompanyService busCompanyService;

        @GetMapping
        @Operation(summary = "Lấy danh sách nhà xe", description = "Lấy danh sách tất cả nhà xe với phân trang và tìm kiếm. "
                        +
                        "Ví dụ: GET /api/user/bus-companies?page=0&size=10&sortBy=createdAt&sortDir=desc")
        public ResponseEntity<ApiResponse> getAllBusCompanies(
                        @Parameter(description = "Tên nhà xe để tìm kiếm") @RequestParam(required = false) String companyName,
                        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sắp xếp theo (id, companyName, createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Hướng sắp xếp (asc, desc)") @RequestParam(defaultValue = "desc") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<BusCompanyResponse> companies = busCompanyService.getAllBusCompanies(companyName, pageable);

                return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà xe thành công", companies));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Lấy thông tin nhà xe", description = "Lấy thông tin chi tiết của một nhà xe theo ID")
        public ResponseEntity<ApiResponse> getBusCompanyById(
                        @Parameter(description = "ID của nhà xe") @PathVariable Integer id) {

                BusCompanyResponse company = busCompanyService.getBusCompanyById(id);
                return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhà xe thành công", company));
        }

        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm nhà xe", description = "Tìm kiếm nhà xe theo ID hoặc tên công ty")
        public ResponseEntity<ApiResponse> searchBusCompanies(
                        @Parameter(description = "Từ khóa tìm kiếm (ID hoặc tên nhà xe)") @RequestParam String searchTerm,
                        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sắp xếp theo (id, companyName, createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Hướng sắp xếp (asc, desc)") @RequestParam(defaultValue = "desc") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<BusCompanyResponse> companies = busCompanyService.searchBusCompanies(searchTerm, pageable);

                return ResponseEntity.ok(ApiResponse.success("Tìm kiếm nhà xe thành công", companies));
        }

        @PostMapping("/register")
        @PreAuthorize("hasRole('USER')")
        @Operation(summary = "Đăng ký nhà xe", description = "Đăng ký nhà xe mới (cần xác thực từ admin)")
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
        public ResponseEntity<ApiResponse> registerBusCompany(
                        @Valid @org.springframework.web.bind.annotation.RequestBody BusCompanyRegistrationRequest request) {

                busCompanyService.registerBusCompany(request);
                return ResponseEntity.ok(ApiResponse.success(
                                "Đăng ký nhà xe thành công. Chúng tôi sẽ xem xét và thông báo kết quả qua email."));
        }

        @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload ảnh cho đăng ký", description = "Upload ảnh logo để sử dụng khi đăng ký nhà xe")
        public ResponseEntity<ApiResponse> uploadImageForRegistration(
                        @Parameter(description = "File ảnh cần upload") @RequestParam("file") MultipartFile file) {

                String imageUrl = busCompanyService.uploadImageForRegistration(file);
                return ResponseEntity.ok(ApiResponse.success("Upload ảnh thành công", Map.of("imageUrl", imageUrl)));
        }
}