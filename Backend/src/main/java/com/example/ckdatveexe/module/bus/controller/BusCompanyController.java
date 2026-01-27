package com.example.ckdatveexe.module.bus.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.bus.dto.*;
import com.example.ckdatveexe.module.bus.service.BusService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.BusStatus;
import com.example.ckdatveexe.shared.entity.DeletedBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bus-company/buses")
@RequiredArgsConstructor
@Tag(name = "Bus Company API", description = "API quản lý xe cho nhà xe")
@SecurityRequirement(name = "Bearer Authentication") // Yêu cầu authentication cho toàn bộ controller
public class BusCompanyController {

        private final BusService busService;

        @PostMapping
        @Operation(summary = "Thêm xe mới", description = "Tạo xe mới và tự động sinh ghế theo loại xe")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> createBus(
                        @Valid @RequestBody BusCreateRequest request,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();

                BusResponse createdBus = busService.createBus(companyId, request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<BusResponse>builder()
                                                .success(true)
                                                .message("Tạo xe thành công")
                                                .data(createdBus)
                                                .build());
        }

        @GetMapping
        @Operation(summary = "Danh sách xe của nhà xe", description = "Lấy danh sách tất cả xe của nhà xe")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(required = false) BusStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();

                BusSearchRequest request = new BusSearchRequest();
                request.setKeyword(keyword);
                request.setStatus(status);
                request.setPage(page);
                request.setSize(size);
                request.setSortBy(sortBy);
                request.setSortDirection(sortDirection);

                Page<BusResponse> buses = busService.getBusesForCompany(companyId, request);

                return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                .success(true)
                                .message("Lấy danh sách xe thành công")
                                .data(buses)
                                .build());
        }

        @GetMapping("/{busId}")
        @Operation(summary = "Chi tiết xe", description = "Lấy thông tin chi tiết của một xe")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> getBusDetail(
                        @PathVariable Integer busId,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();

                BusResponse bus = busService.getBusDetailForCompany(companyId, busId);

                return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                .success(true)
                                .message("Lấy thông tin xe thành công")
                                .data(bus)
                                .build());
        }

        @PutMapping("/{busId}")
        @Operation(summary = "Cập nhật xe", description = "Cập nhật thông tin cơ bản của xe")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> updateBus(
                        @PathVariable Integer busId,
                        @Valid @RequestBody BusUpdateRequest request,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();

                BusResponse updatedBus = busService.updateBusForCompany(companyId, busId, request);

                return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                .success(true)
                                .message("Cập nhật xe thành công")
                                .data(updatedBus)
                                .build());
        }

        @DeleteMapping("/{busId}")
        @Operation(summary = "Xóa xe", description = "Xóa xe (có thể xóa cứng hoặc mềm)")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Void>> deleteBus(
                        @PathVariable Integer busId,
                        @RequestBody(required = false) DeleteBusRequest request,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();
                Integer userId = userDetails.getUser().getId();

                if (request == null) {
                        request = new DeleteBusRequest();
                }

                busService.deleteBus(companyId, busId, request, userId);

                String message = request.isHardDelete() ? "Xóa xe vĩnh viễn thành công" : "Xóa xe thành công";

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message(message)
                                .build());
        }

        @PutMapping("/{busId}/seat-layout")
        @Operation(summary = "Sắp xếp sơ đồ ghế", description = "Cập nhật sơ đồ ghế của xe")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Void>> updateSeatLayout(
                        @PathVariable Integer busId,
                        @Valid @RequestBody SeatLayoutRequest request,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();

                busService.updateSeatLayout(companyId, busId, request);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Cập nhật sơ đồ ghế thành công")
                                .build());
        }

        @PostMapping("/{busId}/regenerate-seats")
        @Operation(summary = "Tái tạo ghế", description = "Tái tạo ghế theo loại xe (xóa ghế cũ và tạo mới)")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Void>> regenerateSeats(
                        @PathVariable Integer busId,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();

                busService.regenerateSeats(companyId, busId);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Tái tạo ghế thành công")
                                .build());
        }

        @GetMapping("/deleted")
        @Operation(summary = "Danh sách xe đã xóa", description = "Lấy danh sách xe đã xóa mềm của nhà xe")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<DeletedBus>>> getDeletedBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "deletedAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();

                BusSearchRequest request = new BusSearchRequest();
                request.setKeyword(keyword);
                request.setPage(page);
                request.setSize(size);
                request.setSortBy(sortBy);
                request.setSortDirection(sortDirection);

                Page<DeletedBus> deletedBuses = busService.getDeletedBuses(companyId, request);

                return ResponseEntity.ok(ApiResponse.<Page<DeletedBus>>builder()
                                .success(true)
                                .message("Lấy danh sách xe đã xóa thành công")
                                .data(deletedBuses)
                                .build());
        }

        @PostMapping("/deleted/{deletedBusId}/restore")
        @Operation(summary = "Khôi phục xe đã xóa", description = "Khôi phục xe từ danh sách đã xóa mềm")
        @PreAuthorize("hasRole('ROLE_BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> restoreDeletedBus(
                        @PathVariable Integer deletedBusId,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Integer companyId = userDetails.getUser().getBusCompany().getId();

                BusResponse restoredBus = busService.restoreDeletedBus(companyId, deletedBusId);

                return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                .success(true)
                                .message("Khôi phục xe thành công")
                                .data(restoredBus)
                                .build());
        }
}