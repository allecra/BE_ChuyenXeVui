package com.example.ckdatveexe.module.route.buscompany.controller;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.route.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.route.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/bus-companies")
@RequiredArgsConstructor
@Tag(name = "Bus Company User API", description = "API xem thông tin nhà xe cho người dùng")
@SecurityRequirement(name = "Bearer Authentication") // Yêu cầu authentication cho toàn bộ controller
@Slf4j
public class BusCompanyUserController {

        private final BusCompanyService busCompanyService;

        @GetMapping
        @Operation(summary = "Lấy danh sách nhà xe", description = "Lấy danh sách nhà xe với phân trang và tìm kiếm")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> getAllBusCompanies(
                        @RequestParam(defaultValue = "") String companyName,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir,
                        Authentication authentication) {

                log.info("👤 [BUS COMPANY USER] GET /api/user/bus-companies - Get all bus companies");
                log.info("🔐 [BUS COMPANY USER] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                                        sortBy);
                        Pageable pageable = PageRequest.of(page, size, sort);

                        Page<BusCompanyResponse> busCompanies = busCompanyService.getAllBusCompanies(companyName,
                                        pageable);

                        log.info("✅ [BUS COMPANY USER] 200 OK - Retrieved {} bus companies",
                                        busCompanies.getTotalElements());
                        return ResponseEntity.ok(ApiResponse.<Page<BusCompanyResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách nhà xe thành công")
                                        .data(busCompanies)
                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY USER] 400 BAD_REQUEST - Invalid search parameters: {}",
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusCompanyResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY USER] 500 INTERNAL_SERVER_ERROR - Failed to get bus companies", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusCompanyResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách nhà xe")
                                                        .build());
                }
        }

        @GetMapping("/{id}")
        @Operation(summary = "Lấy thông tin nhà xe", description = "Lấy thông tin chi tiết của một nhà xe")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusCompanyResponse>> getBusCompanyById(@PathVariable Integer id,
                        Authentication authentication) {

                log.info("👤 [BUS COMPANY USER] GET /api/user/bus-companies/{} - Get bus company detail", id);
                log.info("🔐 [BUS COMPANY USER] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        BusCompanyResponse busCompany = busCompanyService.getBusCompanyById(id);

                        log.info("✅ [BUS COMPANY USER] 200 OK - Retrieved bus company detail for ID: {}", id);
                        return ResponseEntity.ok(ApiResponse.<BusCompanyResponse>builder()
                                        .success(true)
                                        .message("Lấy thông tin nhà xe thành công")
                                        .data(busCompany)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY USER] 404 NOT_FOUND - Bus company not found: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Nhà xe không tồn tại")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY USER] 400 BAD_REQUEST - Invalid company ID: {}", id);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("ID nhà xe không hợp lệ")
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY USER] 500 INTERNAL_SERVER_ERROR - Failed to get bus company: {}", id,
                                        e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusCompanyResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy thông tin nhà xe")
                                                        .build());
                }
        }

        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm nhà xe", description = "Tìm kiếm nhà xe theo ID hoặc tên")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> searchBusCompanies(
                        @RequestParam String searchTerm,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir,
                        Authentication authentication) {

                log.info("👤 [BUS COMPANY USER] GET /api/user/bus-companies/search - Search bus companies with term: {}",
                                searchTerm);
                log.info("🔐 [BUS COMPANY USER] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                                        sortBy);
                        Pageable pageable = PageRequest.of(page, size, sort);

                        Page<BusCompanyResponse> busCompanies = busCompanyService.searchBusCompanies(searchTerm,
                                        pageable);

                        log.info("✅ [BUS COMPANY USER] 200 OK - Found {} bus companies for search term: {}",
                                        busCompanies.getTotalElements(), searchTerm);
                        return ResponseEntity.ok(ApiResponse.<Page<BusCompanyResponse>>builder()
                                        .success(true)
                                        .message("Tìm kiếm nhà xe thành công")
                                        .data(busCompanies)
                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY USER] 400 BAD_REQUEST - Invalid search term: {}", searchTerm);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusCompanyResponse>>builder()
                                                        .success(false)
                                                        .message("Từ khóa tìm kiếm không hợp lệ")
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY USER] 500 INTERNAL_SERVER_ERROR - Failed to search bus companies",
                                        e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusCompanyResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tìm kiếm nhà xe")
                                                        .build());
                }
        }
}