package com.example.ckdatveexe.module.bus.controller;

import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.bus.dto.BusSearchRequest;
import com.example.ckdatveexe.module.bus.dto.BusUpdateRequest;
import com.example.ckdatveexe.module.bus.service.BusService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.BusStatus;
import com.example.ckdatveexe.shared.entity.BusType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/buses")
@RequiredArgsConstructor
@Tag(name = "Bus Admin API", description = "API quản lý xe cho admin")
@SecurityRequirement(name = "Bearer Authentication") // Yêu cầu authentication cho toàn bộ controller
public class BusAdminController {

        private final BusService busService;

        @GetMapping
        @Operation(summary = "Xem tất cả các xe (Admin)", description = "Lấy danh sách tất cả các xe với thông tin chi tiết")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getAllBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(required = false) BusType busType,
                        @RequestParam(required = false) BusStatus status,
                        @RequestParam(required = false) Integer companyId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {

                BusSearchRequest request = new BusSearchRequest();
                request.setKeyword(keyword);
                request.setBusType(busType);
                request.setStatus(status);
                request.setCompanyId(companyId);
                request.setPage(page);
                request.setSize(size);
                request.setSortBy(sortBy);
                request.setSortDirection(sortDirection);

                Page<BusResponse> buses = busService.getAllBusesForAdmin(request);

                return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                .success(true)
                                .message("Lấy danh sách xe thành công")
                                .data(buses)
                                .build());
        }

        @GetMapping("/{busId}")
        @Operation(summary = "Xem chi tiết xe (Admin)", description = "Lấy thông tin chi tiết của một xe")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<BusResponse>> getBusDetail(@PathVariable Integer busId) {
                BusResponse bus = busService.getBusDetailForAdmin(busId);

                return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                .success(true)
                                .message("Lấy thông tin xe thành công")
                                .data(bus)
                                .build());
        }

        @PutMapping("/{busId}")
        @Operation(summary = "Cập nhật xe (Admin)", description = "Cập nhật thông tin cơ bản của xe")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<BusResponse>> updateBus(
                        @PathVariable Integer busId,
                        @Valid @RequestBody BusUpdateRequest request) {

                BusResponse updatedBus = busService.updateBusForAdmin(busId, request);

                return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                .success(true)
                                .message("Cập nhật xe thành công")
                                .data(updatedBus)
                                .build());
        }

        @GetMapping("/company/{companyId}")
        @Operation(summary = "Xe của nhà xe (Admin)", description = "Lấy danh sách xe của một nhà xe cụ thể")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getBusesByCompany(
                        @PathVariable Integer companyId,
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(required = false) BusStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {

                BusSearchRequest request = new BusSearchRequest();
                request.setKeyword(keyword);
                request.setStatus(status);
                request.setPage(page);
                request.setSize(size);
                request.setSortBy(sortBy);
                request.setSortDirection(sortDirection);

                Page<BusResponse> buses = busService.getBusesByCompanyForAdmin(companyId, request);

                return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                .success(true)
                                .message("Lấy danh sách xe của nhà xe thành công")
                                .data(buses)
                                .build());
        }
}