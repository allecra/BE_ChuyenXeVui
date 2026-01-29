package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.bus.dto.BusSearchRequest;
import com.example.ckdatveexe.module.bus.dto.DeleteBusRequest;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.buscompany.dto.BusCompanyUpdateRequest;
import com.example.ckdatveexe.module.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
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
@RequestMapping("/api/admin/bus-company")
@RequiredArgsConstructor
@Tag(name = "Bus Company Admin API", description = "API quản lý nhà xe dành cho admin")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class BusCompanyAdminController {

        private final BusCompanyService busCompanyService;

        @GetMapping("/bus-owner")
        @Operation(summary = "Xe thuộc về nhà xe nào", description = "API để xem xe thuộc về nhà xe nào (dành cho admin)")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<BusCompanyResponse>> getBusCompanyByBusId(
                        @RequestParam Integer busId,
                        Authentication authentication) {

                log.info("👑 [BUS COMPANY ADMIN] GET /api/admin/bus-company/bus-owner - Get bus owner for bus ID: {}",
                                busId);
                log.info("🔐 [BUS COMPANY ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusCompanyResponse company = busCompanyService.getBusCompanyByBusId(busId);

                        log.info("✅ [BUS COMPANY ADMIN] 200 OK - Retrieved bus owner for bus ID: {}", busId);
                        return ResponseEntity.ok(ApiResponse.<BusCompanyResponse>builder()
                                        .success(true)
                                        .message("Lấy thông tin nhà xe sở hữu xe thành công")
                                        .data(company)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY ADMIN] 404 NOT_FOUND - Bus not found: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Xe không tồn tại")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY ADMIN] 400 BAD_REQUEST - Invalid bus ID: {}", busId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("ID xe không hợp lệ")
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get bus owner for ID: {}",
                                        busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy thông tin nhà xe")
                                                        .build());
                }
        }

        @PostMapping("/reset-password")
        @Operation(summary = "Reset mật khẩu nhà xe", description = "Reset mật khẩu cho tài khoản nhà xe và gửi email thông báo")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> resetBusCompanyPassword(
                        @RequestParam String email,
                        Authentication authentication) {

                log.info("👑 [BUS COMPANY ADMIN] POST /api/admin/bus-company/reset-password - Reset password for email: {}",
                                email);
                log.info("🔐 [BUS COMPANY ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        busCompanyService.resetBusCompanyPassword(email);

                        log.info("✅ [BUS COMPANY ADMIN] 200 OK - Password reset successful for email: {}", email);
                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message("Đã reset mật khẩu và gửi email thông báo thành công")
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY ADMIN] 404 NOT_FOUND - Bus company not found for email: {}", email);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy nhà xe với email này")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY ADMIN] 400 BAD_REQUEST - Invalid email: {}", email);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Email không hợp lệ")
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to reset password for email: {}",
                                        email, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi reset mật khẩu")
                                                        .build());
                }
        }

        @GetMapping
        @Operation(summary = "Danh sách tất cả nhà xe", description = "Lấy danh sách tất cả nhà xe (dành cho admin)")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> getAllBusCompanies(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("👑 [BUS COMPANY ADMIN] GET /api/admin/bus-company - Get all bus companies");
                log.info("🔐 [BUS COMPANY ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusSearchRequest request = new BusSearchRequest();
                        request.setKeyword(keyword);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<BusCompanyResponse> companies = busCompanyService.getAllBusCompanies(request);

                        log.info("✅ [BUS COMPANY ADMIN] 200 OK - Retrieved {} bus companies",
                                        companies.getTotalElements());
                        return ResponseEntity.ok(ApiResponse.<Page<BusCompanyResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách nhà xe thành công")
                                        .data(companies)
                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY ADMIN] 400 BAD_REQUEST - Invalid search parameters: {}",
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusCompanyResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get bus companies", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusCompanyResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách nhà xe")
                                                        .build());
                }
        }

        @PutMapping("/{companyId}")
        @Operation(summary = "Cập nhật thông tin nhà xe", description = "Cập nhật thông tin nhà xe và gửi email thông báo")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<BusCompanyResponse>> updateBusCompany(
                        @PathVariable Integer companyId,
                        @Valid @RequestBody BusCompanyUpdateRequest request,
                        Authentication authentication) {

                log.info("👑 [BUS COMPANY ADMIN] PUT /api/admin/bus-company/{} - Update bus company", companyId);
                log.info("🔐 [BUS COMPANY ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusCompanyResponse updatedCompany = busCompanyService.updateBusCompanyByAdmin(companyId,
                                        request);

                        log.info("✅ [BUS COMPANY ADMIN] 200 OK - Bus company updated successfully with ID: {}",
                                        companyId);
                        return ResponseEntity.ok(ApiResponse.<BusCompanyResponse>builder()
                                        .success(true)
                                        .message("Cập nhật thông tin nhà xe thành công và đã gửi email thông báo")
                                        .data(updatedCompany)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY ADMIN] 404 NOT_FOUND - Bus company not found: {}", companyId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Nhà xe không tồn tại")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY ADMIN] 400 BAD_REQUEST - Invalid update data for company {}: {}",
                                        companyId, e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Dữ liệu cập nhật không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to update company: {}",
                                        companyId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi cập nhật nhà xe")
                                                        .build());
                }
        }

        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm nhà xe", description = "Tìm kiếm nhà xe theo từ khóa (dành cho admin)")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> searchBusCompanies(
                        @RequestParam String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("👑 [BUS COMPANY ADMIN] GET /api/admin/bus-company/search - Search bus companies with keyword: {}",
                                keyword);
                log.info("🔐 [BUS COMPANY ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        if (keyword == null || keyword.trim().isEmpty()) {
                                log.error("❌ [BUS COMPANY ADMIN] 400 BAD_REQUEST - Empty search keyword");
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.<Page<BusCompanyResponse>>builder()
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

                        Page<BusCompanyResponse> companies = busCompanyService.searchBusCompanies(request);

                        log.info("✅ [BUS COMPANY ADMIN] 200 OK - Found {} bus companies matching keyword: {}",
                                        companies.getTotalElements(), keyword);
                        return ResponseEntity.ok(ApiResponse.<Page<BusCompanyResponse>>builder()
                                        .success(true)
                                        .message("Tìm kiếm nhà xe thành công")
                                        .data(companies)
                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY ADMIN] 400 BAD_REQUEST - Invalid search parameters: {}",
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusCompanyResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to search bus companies with keyword: {}",
                                        keyword, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusCompanyResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tìm kiếm nhà xe")
                                                        .build());
                }
        }

        @GetMapping("/{companyId}/buses/{busId}")
        @Operation(summary = "Chi tiết xe của nhà xe", description = "Xem chi tiết xe của một nhà xe cụ thể (dành cho admin)")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<BusResponse>> getBusDetailByCompany(
                        @PathVariable Integer companyId,
                        @PathVariable Integer busId,
                        Authentication authentication) {

                log.info("👑 [BUS COMPANY ADMIN] GET /api/admin/bus-company/{}/buses/{} - Get bus detail", companyId,
                                busId);
                log.info("🔐 [BUS COMPANY ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusResponse bus = busCompanyService.getBusDetailByCompanyForAdmin(companyId, busId);

                        log.info("✅ [BUS COMPANY ADMIN] 200 OK - Retrieved bus detail for company {} and bus {}",
                                        companyId, busId);
                        return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                        .success(true)
                                        .message("Lấy chi tiết xe thành công")
                                        .data(bus)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY ADMIN] 404 NOT_FOUND - Bus or company not found: company={}, bus={}",
                                        companyId, busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Xe hoặc nhà xe không tồn tại")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY ADMIN] 400 BAD_REQUEST - Invalid IDs: company={}, bus={}", companyId,
                                        busId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("ID nhà xe hoặc xe không hợp lệ")
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get bus detail: company={}, bus={}",
                                        companyId, busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy chi tiết xe")
                                                        .build());
                }
        }

        @DeleteMapping("/{companyId}/buses/{busId}")
        @Operation(summary = "Xóa xe của nhà xe", description = "Xóa xe của nhà xe (xóa cứng hoặc mềm) - dành cho admin")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteBusOfCompany(
                        @PathVariable Integer companyId,
                        @PathVariable Integer busId,
                        @RequestBody(required = false) DeleteBusRequest request,
                        Authentication authentication) {

                log.info("👑 [BUS COMPANY ADMIN] DELETE /api/admin/bus-company/{}/buses/{} - Delete bus", companyId,
                                busId);
                log.info("🔐 [BUS COMPANY ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        if (request == null) {
                                request = new DeleteBusRequest();
                        }

                        busCompanyService.deleteBusOfCompanyByAdmin(companyId, busId, request);

                        String message = request.isHardDelete() ? "Xóa xe vĩnh viễn thành công" : "Xóa xe thành công";
                        log.info("✅ [BUS COMPANY ADMIN] 200 OK - Bus deleted successfully: company={}, bus={}",
                                        companyId, busId);

                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message(message)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY ADMIN] 404 NOT_FOUND - Bus or company not found for deletion: company={}, bus={}",
                                        companyId, busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Xe hoặc nhà xe không tồn tại")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY ADMIN] 400 BAD_REQUEST - Invalid delete request: company={}, bus={}, error={}",
                                        companyId, busId, e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Yêu cầu xóa không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to delete bus: company={}, bus={}",
                                        companyId, busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi xóa xe")
                                                        .build());
                }
        }

        @PostMapping("/{companyId}/restore-account")
        @Operation(summary = "Khôi phục tài khoản nhà xe", description = "Khôi phục tài khoản nhà xe đã bị xóa mềm (dành cho admin)")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<BusCompanyResponse>> restoreBusCompanyAccount(
                        @PathVariable Integer companyId,
                        Authentication authentication) {

                log.info("👑 [BUS COMPANY ADMIN] POST /api/admin/bus-company/{}/restore-account - Restore company account",
                                companyId);
                log.info("🔐 [BUS COMPANY ADMIN] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusCompanyResponse restoredCompany = busCompanyService.restoreBusCompanyAccount(companyId);

                        log.info("✅ [BUS COMPANY ADMIN] 201 CREATED - Bus company account restored successfully: {}",
                                        companyId);
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(true)
                                                        .message("Khôi phục tài khoản nhà xe thành công")
                                                        .data(restoredCompany)
                                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY ADMIN] 404 NOT_FOUND - Bus company not found for restoration: {}",
                                        companyId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Nhà xe không tồn tại hoặc chưa bị xóa")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY ADMIN] 400 BAD_REQUEST - Invalid restore request for company: {}",
                                        companyId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Yêu cầu khôi phục không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to restore company account: {}",
                                        companyId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi khôi phục tài khoản nhà xe")
                                                        .build());
                }
        }
}