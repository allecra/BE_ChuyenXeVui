package com.example.ckdatveexe.module.bus.controller;

import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.bus.dto.BusSearchRequest;
import com.example.ckdatveexe.module.bus.service.BusService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/buses")
@RequiredArgsConstructor
@Tag(name = "Bus User API", description = "API quản lý xe cho người dùng")
@SecurityRequirements() // Chỉ định không cần security cho toàn bộ controller
public class BusUserController {

        private final BusService busService;

        @GetMapping
        @Operation(summary = "Xem tất cả các xe", description = "Lấy danh sách tất cả các xe khả dụng")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getAllBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {

                BusSearchRequest request = new BusSearchRequest();
                request.setKeyword(keyword);
                request.setPage(page);
                request.setSize(size);
                request.setSortBy(sortBy);
                request.setSortDirection(sortDirection);

                Page<BusResponse> buses = busService.getAllBusesForUser(request);

                return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                .success(true)
                                .message("Lấy danh sách xe thành công")
                                .data(buses)
                                .build());
        }

        @GetMapping("/{busId}")
        @Operation(summary = "Xem chi tiết xe", description = "Lấy thông tin chi tiết của một xe")
        public ResponseEntity<ApiResponse<BusResponse>> getBusDetail(@PathVariable Integer busId) {
                BusResponse bus = busService.getBusDetailForUser(busId);

                return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                .success(true)
                                .message("Lấy thông tin xe thành công")
                                .data(bus)
                                .build());
        }

        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm xe", description = "Tìm kiếm xe theo từ khóa")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> searchBuses(
                        @RequestParam String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {

                BusSearchRequest request = new BusSearchRequest();
                request.setKeyword(keyword);
                request.setPage(page);
                request.setSize(size);
                request.setSortBy(sortBy);
                request.setSortDirection(sortDirection);

                Page<BusResponse> buses = busService.searchBusesForUser(request);

                return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                .success(true)
                                .message("Tìm kiếm xe thành công")
                                .data(buses)
                                .build());
        }

        @GetMapping("/company/{companyId}")
        @Operation(summary = "Xe của nhà xe", description = "Lấy danh sách xe của một nhà xe cụ thể")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getBusesByCompany(
                        @PathVariable Integer companyId,
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {

                BusSearchRequest request = new BusSearchRequest();
                request.setKeyword(keyword);
                request.setPage(page);
                request.setSize(size);
                request.setSortBy(sortBy);
                request.setSortDirection(sortDirection);

                Page<BusResponse> buses = busService.getBusesByCompanyForUser(companyId, request);

                return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                .success(true)
                                .message("Lấy danh sách xe của nhà xe thành công")
                                .data(buses)
                                .build());
        }
}