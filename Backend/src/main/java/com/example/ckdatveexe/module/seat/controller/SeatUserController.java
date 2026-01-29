package com.example.ckdatveexe.module.seat.controller;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.seat.dto.SeatResponse;
import com.example.ckdatveexe.module.seat.dto.SeatSearchRequest;
import com.example.ckdatveexe.module.seat.service.SeatService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.SeatStatus;
import com.example.ckdatveexe.shared.entity.SeatType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/seats")
@RequiredArgsConstructor
@Tag(name = "Seat User API", description = "API quản lý ghế cho người dùng")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class SeatUserController {

    private final SeatService seatService;

    @GetMapping("/bus/{busId}")
    @Operation(summary = "Xem danh sách ghế của xe", description = "Lấy danh sách ghế của một xe (chỉ ghế không bị xóa)")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Page<SeatResponse>>> getSeatsByBus(
            @PathVariable Integer busId,
            @RequestParam(required = false) SeatStatus status,
            @RequestParam(required = false) SeatType seatType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String seatNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "seatNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Authentication authentication) {

        log.info("👤 [USER] GET /api/user/seats/bus/{} - Get seats by bus", busId);
        log.info("🔐 [USER] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
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

            Page<SeatResponse> seats = seatService.getSeatsForUser(busId, request);

            log.info("✅ [USER] 200 OK - Retrieved {} seats for bus ID: {}", seats.getTotalElements(), busId);
            return ResponseEntity.ok(ApiResponse.<Page<SeatResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách ghế thành công")
                    .data(seats)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Bus not found: {}", busId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Page<SeatResponse>>builder()
                            .success(false)
                            .message("Không tìm thấy xe với ID: " + busId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [USER] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<SeatResponse>>builder()
     .success(false)
                            .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get seats for bus: {}", busId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<SeatResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách ghế")
                            .build());
        }
    }

    @GetMapping("/{seatId}")
    @Operation(summary = "Xem chi tiết ghế", description = "Lấy thông tin chi tiết của một ghế")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
    public ResponseEntity<ApiResponse<SeatResponse>> getSeatDetail(
            @PathVariable Integer seatId,
            Authentication authentication) {

        log.info("👤 [USER] GET /api/user/seats/{} - Get seat detail", seatId);
        log.info("🔐 [USER] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            SeatResponse seat = seatService.getSeatDetailForUser(seatId);

            log.info("✅ [USER] 200 OK - Retrieved seat detail for ID: {}", seatId);
            return ResponseEntity.ok(ApiResponse.<SeatResponse>builder()
                    .success(true)
                    .message("Lấy thông tin ghế thành công")
                    .data(seat)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Seat not found: {}", seatId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Không tìm thấy ghế với ID: " + seatId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [USER] 400 BAD_REQUEST - Invalid seat ID: {}", seatId);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("ID ghế không hợp lệ: " + seatId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get seat detail: {}", seatId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SeatResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy thông tin ghế")
                            .build());
        }
    }

    @GetMapping("/available/bus/{busId}")
    @Operation(summary = "Xem ghế trống của xe", description = "Lấy danh sách ghế trống có thể đặt của một xe")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Page<SeatResponse>>> getAvailableSeats(
            @PathVariable Integer busId,
            @RequestParam(required = false) SeatType seatType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "seatNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Authentication authentication) {

        log.info("👤 [USER] GET /api/user/seats/available/bus/{} - Get available seats", busId);
        log.info("🔐 [USER] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            SeatSearchRequest request = new SeatSearchRequest();
            request.setBusId(busId);
            request.setStatus(SeatStatus.AVAILABLE); // Only available seats
            request.setSeatType(seatType);
            request.setMinPrice(minPrice);
            request.setMaxPrice(maxPrice);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<SeatResponse> seats = seatService.getSeatsForUser(busId, request);

            log.info("✅ [USER] 200 OK - Retrieved {} available seats for bus ID: {}", seats.getTotalElements(), busId);
            return ResponseEntity.ok(ApiResponse.<Page<SeatResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách ghế trống thành công")
                    .data(seats)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Bus not found: {}", busId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Page<SeatResponse>>builder()
                            .success(false)
                            .message("Không tìm thấy xe với ID: " + busId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get available seats for bus: {}", busId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<SeatResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách ghế trống")
                            .build());
        }
    }
}