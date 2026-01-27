package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.module.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/bus-companies")
@RequiredArgsConstructor
@Tag(name = "Bus Company User API", description = "API xem thông tin nhà xe cho người dùng")
@SecurityRequirements() // Chỉ định không cần security cho toàn bộ controller
public class BusCompanyUserController {

        private final BusCompanyService busCompanyService;

        @GetMapping
        @Operation(summary = "Lấy danh sách nhà xe", description = "Lấy danh sách nhà xe với phân trang và tìm kiếm")
        public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> getAllBusCompanies(
                        @RequestParam(defaultValue = "") String companyName,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {

                Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                                sortBy);
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<BusCompanyResponse> busCompanies = busCompanyService.getAllBusCompanies(companyName, pageable);

                return ResponseEntity.ok(ApiResponse.<Page<BusCompanyResponse>>builder()
                                .success(true)
                                .message("Lấy danh sách nhà xe thành công")
                                .data(busCompanies)
                                .build());
        }

        @GetMapping("/{id}")
        @Operation(summary = "Lấy thông tin nhà xe", description = "Lấy thông tin chi tiết của một nhà xe")
        public ResponseEntity<ApiResponse<BusCompanyResponse>> getBusCompanyById(@PathVariable Integer id) {
                BusCompanyResponse busCompany = busCompanyService.getBusCompanyById(id);

                return ResponseEntity.ok(ApiResponse.<BusCompanyResponse>builder()
                                .success(true)
                                .message("Lấy thông tin nhà xe thành công")
                                .data(busCompany)
                                .build());
        }

        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm nhà xe", description = "Tìm kiếm nhà xe theo ID hoặc tên")
        public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> searchBusCompanies(
                        @RequestParam String searchTerm,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {

                Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                                sortBy);
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<BusCompanyResponse> busCompanies = busCompanyService.searchBusCompanies(searchTerm, pageable);

                return ResponseEntity.ok(ApiResponse.<Page<BusCompanyResponse>>builder()
                                .success(true)
                                .message("Tìm kiếm nhà xe thành công")
                                .data(busCompanies)
                                .build());
        }
}