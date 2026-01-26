package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyCreateRequest;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyUpdateRequest;
import com.example.ckdatveexe.module.buscompany.dto.RegistrationActionRequest;
import com.example.ckdatveexe.module.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.BusCompanyRegistration;
import com.example.ckdatveexe.shared.entity.RegistrationStatus;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/bus-companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Bus Company Admin", description = "API quản lý nhà xe cho admin")
public class BusCompanyAdminController {

        private final BusCompanyService busCompanyService;

        // CRUD Operations for BusCompany
        @GetMapping
        @Operation(summary = "Lấy danh sách nhà xe", description = "Lấy danh sách tất cả nhà xe với phân trang và tìm kiếm (Admin)")
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
        @Operation(summary = "Lấy thông tin nhà xe", description = "Lấy thông tin chi tiết của một nhà xe")
        public ResponseEntity<ApiResponse> getBusCompanyById(
                        @Parameter(description = "ID của nhà xe") @PathVariable Integer id) {

                BusCompanyResponse company = busCompanyService.getBusCompanyById(id);
                return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhà xe thành công", company));
        }

        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm nhà xe", description = "Tìm kiếm nhà xe theo ID hoặc tên công ty (Admin)")
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

        @PostMapping
        @Operation(summary = "Tạo nhà xe mới", description = "Tạo nhà xe mới (Admin only)")
        @RequestBody(description = "Thông tin nhà xe mới", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Tạo nhà xe mới", description = "Admin tạo nhà xe mới với đầy đủ thông tin", value = """
                        {
                          "companyName": "Nhà xe Hoàng Long",
                          "image": "https://example.com/hoang-long-logo.jpg",
                          "descriptions": "Nhà xe Hoàng Long chuyên phục vụ tuyến Hà Nội - Lào Cai với xe giường nằm cao cấp."
                        }
                        """)))
        public ResponseEntity<ApiResponse> createBusCompany(
                        @Valid @org.springframework.web.bind.annotation.RequestBody BusCompanyCreateRequest request) {

                BusCompanyResponse company = busCompanyService.createBusCompany(request);
                return ResponseEntity.ok(ApiResponse.success("Tạo nhà xe thành công", company));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Cập nhật nhà xe", description = "Cập nhật thông tin nhà xe (Admin only)")
        @RequestBody(description = "Thông tin cập nhật nhà xe (các trường không bắt buộc)", content = @Content(mediaType = "application/json", examples = {
                        @ExampleObject(name = "Cập nhật mô tả", description = "Chỉ cập nhật mô tả nhà xe", value = """
                                        {
                                          "descriptions": "Nhà xe Hoàng Long - Cập nhật: Hiện có thêm tuyến Hà Nội - Sapa với xe limousine 9 chỗ."
                                        }
                                        """),
                        @ExampleObject(name = "Cập nhật đầy đủ", description = "Cập nhật tất cả thông tin", value = """
                                        {
                                          "companyName": "Nhà xe Hoàng Long Express",
                                          "image": "https://example.com/new-logo.jpg",
                                          "descriptions": "Nhà xe Hoàng Long Express - Dịch vụ vận chuyển hành khách cao cấp."
                                        }
                                        """)
        }))
        public ResponseEntity<ApiResponse> updateBusCompany(
                        @Parameter(description = "ID của nhà xe") @PathVariable Integer id,
                        @Valid @org.springframework.web.bind.annotation.RequestBody BusCompanyUpdateRequest request) {

                BusCompanyResponse company = busCompanyService.updateBusCompany(id, request);
                return ResponseEntity.ok(ApiResponse.success("Cập nhật nhà xe thành công", company));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Xóa nhà xe", description = "Xóa nhà xe")
        public ResponseEntity<ApiResponse> deleteBusCompany(
                        @Parameter(description = "ID của nhà xe") @PathVariable Integer id) {

                busCompanyService.deleteBusCompany(id);
                return ResponseEntity.ok(ApiResponse.success("Xóa nhà xe thành công"));
        }

        @PostMapping(value = "/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload ảnh nhà xe", description = "Upload ảnh logo cho nhà xe qua Cloudinary")
        public ResponseEntity<ApiResponse> uploadBusCompanyImage(
                        @Parameter(description = "ID của nhà xe") @PathVariable Integer id,
                        @Parameter(description = "File ảnh cần upload") @RequestParam("file") MultipartFile file) {

                String imageUrl = busCompanyService.uploadBusCompanyImage(id, file);
                return ResponseEntity.ok(ApiResponse.success("Upload ảnh thành công", Map.of("imageUrl", imageUrl)));
        }

        // Registration Management
        @GetMapping("/registrations")
        @Operation(summary = "Lấy danh sách đăng ký", description = "Lấy danh sách đăng ký nhà xe với filter và phân trang")
        public ResponseEntity<ApiResponse> getAllRegistrations(
                        @Parameter(description = "Trạng thái đăng ký") @RequestParam(required = false) RegistrationStatus status,
                        @Parameter(description = "Tên nhà xe để tìm kiếm") @RequestParam(required = false) String companyName,
                        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sắp xếp theo (id, companyName, createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Hướng sắp xếp (asc, desc)") @RequestParam(defaultValue = "desc") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<BusCompanyRegistration> registrations = busCompanyService.getAllRegistrations(status, companyName,
                                pageable);

                return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đăng ký thành công", registrations));
        }

        @GetMapping("/registrations/search")
        @Operation(summary = "Tìm kiếm đăng ký", description = "Tìm kiếm đăng ký nhà xe theo ID hoặc tên công ty")
        public ResponseEntity<ApiResponse> searchRegistrations(
                        @Parameter(description = "Trạng thái đăng ký") @RequestParam(required = false) RegistrationStatus status,
                        @Parameter(description = "Từ khóa tìm kiếm (ID hoặc tên nhà xe)") @RequestParam String searchTerm,
                        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sắp xếp theo (id, companyName, createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Hướng sắp xếp (asc, desc)") @RequestParam(defaultValue = "desc") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<BusCompanyRegistration> registrations = busCompanyService.searchRegistrations(status, searchTerm,
                                pageable);

                return ResponseEntity.ok(ApiResponse.success("Tìm kiếm đăng ký thành công", registrations));
        }

        @GetMapping("/registrations/{id}")
        @Operation(summary = "Lấy thông tin đăng ký", description = "Lấy thông tin chi tiết của một đăng ký")
        public ResponseEntity<ApiResponse> getRegistrationById(
                        @Parameter(description = "ID của đăng ký") @PathVariable Integer id) {

                BusCompanyRegistration registration = busCompanyService.getRegistrationById(id);
                return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đăng ký thành công", registration));
        }

        @PostMapping("/registrations/{id}/approve")
        @Operation(summary = "Duyệt đăng ký", description = "Duyệt đăng ký nhà xe và tạo BusCompany mới")
        @RequestBody(description = "Ghi chú của admin (không bắt buộc)", content = @Content(mediaType = "application/json", examples = {
                        @ExampleObject(name = "Duyệt với ghi chú", description = "Duyệt đăng ký kèm ghi chú", value = """
                                        {
                                          "adminNotes": "Đăng ký hợp lệ. Đã kiểm tra giấy phép kinh doanh và thông tin liên hệ."
                                        }
                                        """),
                        @ExampleObject(name = "Duyệt không ghi chú", description = "Duyệt đăng ký không cần ghi chú", value = "{}")
        }))
        public ResponseEntity<ApiResponse> approveRegistration(
                        @Parameter(description = "ID của đăng ký") @PathVariable Integer id,
                        @org.springframework.web.bind.annotation.RequestBody(required = false) RegistrationActionRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                String adminNotes = request != null ? request.getAdminNotes() : null;
                busCompanyService.approveRegistration(id, userDetails.getId(), adminNotes);
                return ResponseEntity.ok(ApiResponse.success("Duyệt đăng ký thành công"));
        }

        @PostMapping("/registrations/{id}/reject")
        @Operation(summary = "Từ chối đăng ký", description = "Từ chối đăng ký nhà xe với lý do")
        @RequestBody(description = "Lý do từ chối (khuyến khích có)", content = @Content(mediaType = "application/json", examples = {
                        @ExampleObject(name = "Từ chối với lý do", description = "Từ chối đăng ký kèm lý do cụ thể", value = """
                                        {
                                          "adminNotes": "Giấy phép kinh doanh không hợp lệ. Vui lòng cung cấp giấy phép kinh doanh vận tải hành khách còn hiệu lực."
                                        }
                                        """),
                        @ExampleObject(name = "Từ chối thông tin thiếu", description = "Từ chối do thiếu thông tin", value = """
                                        {
                                          "adminNotes": "Thông tin đăng ký chưa đầy đủ. Vui lòng bổ sung địa chỉ trụ sở chính và số điện thoại liên hệ."
                                        }
                                        """)
        }))
        public ResponseEntity<ApiResponse> rejectRegistration(
                        @Parameter(description = "ID của đăng ký") @PathVariable Integer id,
                        @org.springframework.web.bind.annotation.RequestBody(required = false) RegistrationActionRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                String adminNotes = request != null ? request.getAdminNotes() : null;
                busCompanyService.rejectRegistration(id, userDetails.getId(), adminNotes);
                return ResponseEntity.ok(ApiResponse.success("Từ chối đăng ký thành công"));
        }
}