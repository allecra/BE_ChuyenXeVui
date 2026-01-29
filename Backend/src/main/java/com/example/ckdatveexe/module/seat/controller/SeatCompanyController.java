package com.example.ckdatveexe.module.seat.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.seat.dto.*;
import com.example.ckdatveexe.module.seat.service.SeatService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.SeatStatus;
import com.example.ckdatveexe.shared.entity.SeatType;
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
@RequestMapping("/api/bus-company/seats")
@RequiredArgsConstructor
@Tag(name = "Seat Company API", description = "API quản lý ghế cho nhà xe")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class SeatCompanyController {

    private final SeatService seatService;

    @PostMapping
    @Operation(summary = "Tạo ghế mới", description = "Tạo ghế mới cho xe thuộc nhà xe")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<SeatResponse>> createSeat(
            @Valid @RequestBody SeatCreateRequest request,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] POST /api/bus-company/seats - Create seat");
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Integer companyId = userDetails.getUser().getBusCompany().getId();

            SeatResponse seat = seatService.createSeat(companyId, request);

            log.info("✅ [BUS COMPANY] 201 CREATED - Seat created successfully: {}", seat.getSeatNumber());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(true)
                            .message("Tạo ghế thành công")
                            .data(seat)
                            .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Bus not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid seat data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to create seat", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi tạo ghế")
                            .build());
        }
    }

    @GetMapping
    @Operation(summary = "Danh sách ghế của nhà xe", description = "Lấy danh sách tất cả ghế thuộc nhà xe (bao gồm ghế đã xóa)")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Page<SeatResponse>>> getSeats(
            @RequestParam(required = false) Integer busId,
            @RequestParam(required = false) SeatStatus status,
            @RequestParam(required = false) SeatType seatType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String seatNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "seatNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] GET /api/bus-company/seats - Get seats");
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Integer companyId = userDetails.getUser().getBusCompany().getId();

            SeatSearchRequest request = new SeatSearchRequest();
            request.setBusId(busId);
            request.setStatus(status);
            request.setSeatType(seatType);
            request.setMinPrice(minPrice);
            request.setMaxPrice(maxPrice);
            request.setSeatNumber(seatNumber);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<SeatResponse> seats = seatService.getSeatsForCompany(companyId, request);

            log.info("✅ [BUS COMPANY] 200 OK - Retrieved {} seats for company", seats.getTotalElements());
            return ResponseEntity.ok(ApiResponse.<Page<SeatResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách ghế thành công")
                    .data(seats)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<SeatResponse>>builder()
                            .success(false)
                            .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get seats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<SeatResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách ghế")
                            .build());
        }
    }

    @GetMapping("/{seatId}")
    @Operation(summary = "Chi tiết ghế", description = "Lấy thông tin chi tiết của một ghế")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<SeatResponse>> getSeatDetail(
            @PathVariable Integer seatId,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] GET /api/bus-company/seats/{} - Get seat detail", seatId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Integer companyId = userDetails.getUser().getBusCompany().getId();

            SeatResponse seat = seatService.getSeatDetailForCompany(companyId, seatId);

            log.info("✅ [BUS COMPANY] 200 OK - Retrieved seat detail for ID: {}", seatId);
            return ResponseEntity.ok(ApiResponse.<SeatResponse>builder()
                    .success(true)
                    .message("Lấy thông tin ghế thành công")
                    .data(seat)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Seat not found: {}", seatId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Không tìm thấy ghế với ID: " + seatId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get seat detail: {}", seatId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy thông tin ghế")
                            .build());
        }
    }

    @PutMapping("/{seatId}")
    @Operation(summary = "Cập nhật thông tin ghế", description = "Cập nhật thông tin cơ bản của ghế")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<SeatResponse>> updateSeat(
            @PathVariable Integer seatId,
            @Valid @RequestBody SeatUpdateRequest request,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] PUT /api/bus-company/seats/{} - Update seat", seatId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Integer companyId = userDetails.getUser().getBusCompany().getId();

            SeatResponse seat = seatService.updateSeat(companyId, seatId, request);

            log.info("✅ [BUS COMPANY] 200 OK - Seat updated successfully: {}", seat.getSeatNumber());
            return ResponseEntity.ok(ApiResponse.<SeatResponse>builder()
                    .success(true)
                    .message("Cập nhật ghế thành công")
                    .data(seat)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Seat not found: {}", seatId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Không tìm thấy ghế với ID: " + seatId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid update data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update seat: {}", seatId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi cập nhật ghế")
                            .build());
        }
    }

    @PutMapping("/{seatId}/status")
    @Operation(summary = "Cập nhật trạng thái ghế", description = "Thay đổi trạng thái ghế (AVAILABLE, BOOKED, MAINTENANCE)")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<SeatResponse>> updateSeatStatus(
            @PathVariable Integer seatId,
            @Valid @RequestBody SeatStatusUpdateRequest request,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] PUT /api/bus-company/seats/{}/status - Update seat status", seatId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Integer companyId = userDetails.getUser().getBusCompany().getId();

            SeatResponse seat = seatService.updateSeatStatus(companyId, seatId, request);

            log.info("✅ [BUS COMPANY] 200 OK - Seat status updated successfully: {} to {}",
                    seat.getSeatNumber(), request.getStatus());
            return ResponseEntity.ok(ApiResponse.<SeatResponse>builder()
                    .success(true)
                    .message("Cập nhật trạng thái ghế thành công")
                    .data(seat)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Seat not found: {}", seatId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Không tìm thấy ghế với ID: " + seatId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid status update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update seat status: {}", seatId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi cập nhật trạng thái ghế")
                            .build());
        }
    }

    @PutMapping("/{seatId}/price")
    @Operation(summary = "Cập nhật giá ghế", description = "Thay đổi giá của ghế")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<SeatResponse>> updateSeatPrice(
            @PathVariable Integer seatId,
            @Valid @RequestBody SeatPriceUpdateRequest request,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] PUT /api/bus-company/seats/{}/price - Update seat price", seatId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Integer companyId = userDetails.getUser().getBusCompany().getId();

            SeatResponse seat = seatService.updateSeatPrice(companyId, seatId, request);

            log.info("✅ [BUS COMPANY] 200 OK - Seat price updated successfully: {} to {}",
                    seat.getSeatNumber(), request.getPriceForSeatType());
            return ResponseEntity.ok(ApiResponse.<SeatResponse>builder()
                    .success(true)
                    .message("Cập nhật giá ghế thành công")
                    .data(seat)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Seat not found: {}", seatId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Không tìm thấy ghế với ID: " + seatId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid price update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update seat price: {}", seatId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi cập nhật giá ghế")
                            .build());
        }
    }

    @DeleteMapping("/{seatId}")
    @Operation(summary = "Xóa ghế (soft delete)", description = "Xóa mềm ghế - chuyển trạng thái thành DELETED")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Void>> deleteSeat(
            @PathVariable Integer seatId,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] DELETE /api/bus-company/seats/{} - Delete seat", seatId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Integer companyId = userDetails.getUser().getBusCompany().getId();

            seatService.deleteSeat(companyId, seatId);

            log.info("✅ [BUS COMPANY] 200 OK - Seat deleted successfully: {}", seatId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Xóa ghế thành công")
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Seat not found: {}", seatId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Không tìm thấy ghế với ID: " + seatId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Cannot delete seat: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to delete seat: {}", seatId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi xóa ghế")
                            .build());
        }
    }
}