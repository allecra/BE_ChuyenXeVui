package com.example.ckdatveexe.module.bus.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.exception.ResourceNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class BusCompanyController {

        private final BusService busService;

        @PostMapping
        @Operation(summary = "Thêm xe mới", description = "Tạo xe mới và tự động sinh ghế theo loại xe")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> createBus(
                        @Valid @RequestBody BusCreateRequest request,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] POST /api/bus-company/buses - Create bus");
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        BusResponse createdBus = busService.createBus(companyId, request);

                        log.info("✅ [BUS COMPANY] 201 CREATED - Bus created successfully with ID: {}",
                                        createdBus.getId());
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(true)
                                                        .message("Tạo xe thành công")
                                                        .data(createdBus)
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid bus data: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Dữ liệu xe không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to create bus", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tạo xe")
                                                        .build());
                }
        }

        @GetMapping
        @Operation(summary = "Danh sách xe của nhà xe", description = "Lấy danh sách tất cả xe của nhà xe")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<BusResponse>>> getBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(required = false) BusStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] GET /api/bus-company/buses - Get buses list");
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
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

                        log.info("✅ [BUS COMPANY] 200 OK - Retrieved {} buses for company ID: {}",
                                        buses.getTotalElements(), companyId);
                        return ResponseEntity.ok(ApiResponse.<Page<BusResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách xe thành công")
                                        .data(buses)
                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get buses", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách xe")
                                                        .build());
                }
        }

        @GetMapping("/{busId}")
        @Operation(summary = "Chi tiết xe", description = "Lấy thông tin chi tiết của một xe")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> getBusDetail(
                        @PathVariable Integer busId,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] GET /api/bus-company/buses/{} - Get bus detail", busId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        if (authentication == null || authentication.getPrincipal() == null) {
                                log.error("🚫 [BUS COMPANY] 401 UNAUTHORIZED - Authentication required");
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(ApiResponse.<BusResponse>builder()
                                                                .success(false)
                                                                .message("Yêu cầu đăng nhập")
                                                                .build());
                        }

                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        if (userDetails.getUser() == null || userDetails.getUser().getBusCompany() == null) {
                                log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Bus company information not found");
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.<BusResponse>builder()
                                                                .success(false)
                                                                .message("Thông tin nhà xe không tìm thấy")
                                                                .build());
                        }

                        Integer companyId = userDetails.getUser().getBusCompany().getId();
                        BusResponse bus = busService.getBusDetailForCompany(companyId, busId);

                        log.info("✅ [BUS COMPANY] 200 OK - Retrieved bus detail for ID: {}", busId);
                        return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                        .success(true)
                                        .message("Lấy thông tin xe thành công")
                                        .data(bus)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Bus not found: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Xe không tồn tại")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid bus ID: {}", busId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("ID xe không hợp lệ")
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get bus detail for ID: {}",
                                        busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy thông tin xe")
                                                        .build());
                }
        }

        @PutMapping("/{busId}")
        @Operation(summary = "Cập nhật xe", description = "Cập nhật thông tin cơ bản của xe")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> updateBus(
                        @PathVariable Integer busId,
                        @Valid @RequestBody BusUpdateRequest request,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] PUT /api/bus-company/buses/{} - Update bus", busId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        BusResponse updatedBus = busService.updateBusForCompany(companyId, busId, request);

                        log.info("✅ [BUS COMPANY] 200 OK - Bus updated successfully with ID: {}", busId);
                        return ResponseEntity.ok(ApiResponse.<BusResponse>builder()
                                        .success(true)
                                        .message("Cập nhật xe thành công")
                                        .data(updatedBus)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Bus not found for update: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Xe không tồn tại")
                                                        .build());
                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid update data for bus: {}", busId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Dữ liệu cập nhật không hợp lệ: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update bus: {}", busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi cập nhật xe")
                                                        .build());
                }
        }

        @DeleteMapping("/{busId}")
        @Operation(summary = "Xóa xe", description = "Xóa xe (có thể xóa cứng hoặc mềm)")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Void>> deleteBus(
                        @PathVariable Integer busId,
                        @RequestBody(required = false) DeleteBusRequest request,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] DELETE /api/bus-company/buses/{} - Delete bus", busId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();
                        Integer userId = userDetails.getUser().getId();

                        if (request == null) {
                                request = new DeleteBusRequest();
                        }

                        busService.deleteBus(companyId, busId, request, userId);

                        String message = request.isHardDelete() ? "Xóa xe vĩnh viễn thành công" : "Xóa xe thành công";
                        log.info("✅ [BUS COMPANY] 200 OK - Bus deleted successfully with ID: {}", busId);

                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message(message)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Bus not found for deletion: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Xe không tồn tại")
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid delete request for bus {}: {}", busId,
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Yêu cầu xóa không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to delete bus: {}", busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi xóa xe")
                                                        .build());
                }
        }

        @PutMapping("/{busId}/seat-layout")
        @Operation(summary = "Sắp xếp sơ đồ ghế", description = "Cập nhật sơ đồ ghế của xe")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Void>> updateSeatLayout(
                        @PathVariable Integer busId,
                        @Valid @RequestBody SeatLayoutRequest request,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] PUT /api/bus-company/buses/{}/seat-layout - Update seat layout", busId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        busService.updateSeatLayout(companyId, busId, request);

                        log.info("✅ [BUS COMPANY] 200 OK - Seat layout updated successfully for bus ID: {}", busId);
                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message("Cập nhật sơ đồ ghế thành công")
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Bus not found for seat layout update: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Xe không tồn tại")
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid seat layout data for bus {}: {}", busId,
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Dữ liệu sơ đồ ghế không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update seat layout for bus: {}",
                                        busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi cập nhật sơ đồ ghế")
                                                        .build());
                }
        }

        @PostMapping("/{busId}/regenerate-seats")
        @Operation(summary = "Tái tạo ghế", description = "Tái tạo ghế theo loại xe (xóa ghế cũ và tạo mới)")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Void>> regenerateSeats(
                        @PathVariable Integer busId,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] POST /api/bus-company/buses/{}/regenerate-seats - Regenerate seats", busId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        busService.regenerateSeats(companyId, busId);

                        log.info("✅ [BUS COMPANY] 201 CREATED - Seats regenerated successfully for bus ID: {}", busId);
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(true)
                                                        .message("Tái tạo ghế thành công")
                                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Bus not found for seat regeneration: {}", busId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Xe không tồn tại")
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid bus for seat regeneration: {}", busId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Xe không hợp lệ để tái tạo ghế: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to regenerate seats for bus: {}",
                                        busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tái tạo ghế")
                                                        .build());
                }
        }

        @GetMapping("/deleted")
        @Operation(summary = "Danh sách xe đã xóa", description = "Lấy danh sách xe đã xóa mềm của nhà xe")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<DeletedBus>>> getDeletedBuses(
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "deletedAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] GET /api/bus-company/buses/deleted - Get deleted buses");
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        BusSearchRequest request = new BusSearchRequest();
                        request.setKeyword(keyword);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<DeletedBus> deletedBuses = busService.getDeletedBuses(companyId, request);

                        log.info("✅ [BUS COMPANY] 200 OK - Retrieved {} deleted buses",
                                        deletedBuses.getTotalElements());
                        return ResponseEntity.ok(ApiResponse.<Page<DeletedBus>>builder()
                                        .success(true)
                                        .message("Lấy danh sách xe đã xóa thành công")
                                        .data(deletedBuses)
                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<DeletedBus>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get deleted buses", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<DeletedBus>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách xe đã xóa")
                                                        .build());
                }
        }

        @PostMapping("/deleted/{deletedBusId}/restore")
        @Operation(summary = "Khôi phục xe đã xóa", description = "Khôi phục xe từ danh sách đã xóa mềm")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<BusResponse>> restoreDeletedBus(
                        @PathVariable Integer deletedBusId,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] POST /api/bus-company/buses/deleted/{}/restore - Restore deleted bus",
                                deletedBusId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Integer companyId = userDetails.getUser().getBusCompany().getId();

                        BusResponse restoredBus = busService.restoreDeletedBus(companyId, deletedBusId);

                        log.info("✅ [BUS COMPANY] 201 CREATED - Bus restored successfully with ID: {}", deletedBusId);
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(true)
                                                        .message("Khôi phục xe thành công")
                                                        .data(restoredBus)
                                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Deleted bus not found: {}", deletedBusId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Xe đã xóa không tồn tại")
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid restore request for deleted bus: {}",
                                        deletedBusId);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Yêu cầu khôi phục không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to restore deleted bus: {}",
                                        deletedBusId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi khôi phục xe")
                                                        .build());
                }
        }
}