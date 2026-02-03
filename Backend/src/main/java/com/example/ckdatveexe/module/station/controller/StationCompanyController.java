package com.example.ckdatveexe.module.station.controller;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.station.dto.*;
import com.example.ckdatveexe.module.station.service.StationService;
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

import java.util.List;

@RestController
@RequestMapping("/api/bus-company/stations")
@RequiredArgsConstructor
@Tag(name = "Station Company API", description = "API quản lý bến xe cho nhà xe")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class StationCompanyController {

        private final StationService stationService;

        @PostMapping
        @Operation(summary = "Tạo bến xe mới", description = "Tạo bến xe mới trong hệ thống")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<StationResponse>> createStation(
                        @Valid @RequestBody StationCreateRequest request,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] POST /api/bus-company/stations - Create station");
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        StationResponse station = stationService.createStation(request);

                        log.info("🆕 [BUS COMPANY] 201 CREATED - Station created successfully: {}", station.getName());
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(true)
                                                        .message("Tạo bến xe thành công")
                                                        .data(station)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid station data: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to create station", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tạo bến xe")
                                                        .build());
                }
        }

        @GetMapping
        @Operation(summary = "Danh sách tất cả bến xe", description = "Lấy danh sách tất cả bến xe (dành cho nhà xe)")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<StationResponse>>> getAllStations(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String location,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "name") String sortBy,
                        @RequestParam(defaultValue = "asc") String sortDirection,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] GET /api/bus-company/stations - Get all stations");
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        StationSearchRequest request = new StationSearchRequest();
                        request.setKeyword(keyword);
                        request.setLocation(location);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<StationResponse> stations = stationService.getAllStationsForCompany(request);

                        log.info("✅ [BUS COMPANY] 200 OK - Retrieved {} stations", stations.getTotalElements());
                        return ResponseEntity.ok(ApiResponse.<Page<StationResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách bến xe thành công")
                                        .data(stations)
                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<StationResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get stations", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<StationResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách bến xe")
                                                        .build());
                }
        }

        @GetMapping("/{stationId}")
        @Operation(summary = "Chi tiết bến xe", description = "Lấy thông tin chi tiết của một bến xe")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<StationResponse>> getStationDetail(
                        @PathVariable Integer stationId,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] GET /api/bus-company/stations/{} - Get station detail", stationId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        StationResponse station = stationService.getStationDetailForCompany(stationId);

                        log.info("✅ [BUS COMPANY] 200 OK - Retrieved station detail for ID: {}", stationId);
                        return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                                        .success(true)
                                        .message("Lấy thông tin bến xe thành công")
                                        .data(station)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Station not found: {}", stationId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy bến xe với ID: " + stationId)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get station detail: {}",
                                        stationId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy thông tin bến xe")
                                                        .build());
                }
        }

        @PutMapping("/{stationId}")
        @Operation(summary = "Cập nhật bến xe", description = "Cập nhật thông tin bến xe")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<StationResponse>> updateStation(
                        @PathVariable Integer stationId,
                        @Valid @RequestBody StationUpdateRequest request,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] PUT /api/bus-company/stations/{} - Update station", stationId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        StationResponse station = stationService.updateStation(stationId, request);

                        log.info("✅ [BUS COMPANY] 200 OK - Station updated successfully: {}", station.getName());
                        return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                                        .success(true)
                                        .message("Cập nhật bến xe thành công")
                                        .data(station)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Station not found: {}", stationId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy bến xe với ID: " + stationId)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid update data: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update station: {}",
                                        stationId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi cập nhật bến xe")
                                                        .build());
                }
        }

        @PostMapping("/assign-buses-detailed")
        @Operation(summary = "Gắn xe cho bến (chi tiết)", description = "Gắn danh sách xe vào bến xe với thông tin chi tiết (notes, trạng thái)")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin gắn xe vào bến với chi tiết đầy đủ", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
                        @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Thêm xe với ghi chú chi tiết", summary = "Thêm xe với thông tin chi tiết và ghi chú", description = "Thêm xe vào bến với notes và trạng thái cụ thể", value = """
                                        {
                                          "stationId": 1,
                                          "replaceAll": false,
                                          "busStations": [
                                            {
                                              "busId": 3,
                                              "stationId": 1,
                                              "notes": "Xe chính tuyến Hà Nội - Hồ Chí Minh, hoạt động 24/7",
                                              "isActive": true
                                            },
                                            {
                                              "busId": 4,
                                              "stationId": 1,
                                              "notes": "Xe dự phòng, chỉ hoạt động cuối tuần và ngày lễ",
                                              "isActive": true
                                            }
                                          ]
                                        }
                                        """),
                        @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Thay thế với xe bảo trì", summary = "Thay thế tất cả xe và đánh dấu xe bảo trì", description = "Thay thế tất cả xe tại bến, một số xe đang bảo trì", value = """
                                        {
                                          "stationId": 2,
                                          "replaceAll": true,
                                          "busStations": [
                                            {
                                              "busId": 5,
                                              "stationId": 2,
                                              "notes": "Xe mới, vừa qua kiểm định",
                                              "isActive": true
                                            },
                                            {
                                              "busId": 6,
                                              "stationId": 2,
                                              "notes": "Xe đang bảo trì định kỳ, tạm ngưng hoạt động",
                                              "isActive": false
                                            }
                                          ]
                                        }
                                        """),
                        @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Cập nhật thông tin xe hiện có", summary = "Cập nhật notes cho xe đã có tại bến", description = "Cập nhật ghi chú cho xe đã tồn tại tại bến", value = """
                                        {
                                          "stationId": 1,
                                          "replaceAll": false,
                                          "busStations": [
                                            {
                                              "busId": 3,
                                              "stationId": 1,
                                              "notes": "Đã cập nhật lịch trình mới - tăng tần suất chạy",
                                              "isActive": true
                                            }
                                          ]
                                        }
                                        """)
        }))
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<StationResponse>> assignBusesToStationDetailed(
                        @Valid @RequestBody BusStationDetailRequest request,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] POST /api/bus-company/stations/assign-buses-detailed - Assign buses to station with details");
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        StationResponse station = stationService.assignBusesToStationDetailed(request);

                        log.info("✅ [BUS COMPANY] 200 OK - Assigned {} buses with details to station: {}",
                                        request.getBusStations().size(), station.getName());
                        return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                                        .success(true)
                                        .message("Gắn xe vào bến với thông tin chi tiết thành công")
                                        .data(station)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Station or buses not found: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid detailed assign request: {}",
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to assign buses with details to station",
                                        e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi gắn xe vào bến với thông tin chi tiết")
                                                        .build());
                }
        }

        @GetMapping("/{stationId}/bus-stations")
        @Operation(summary = "Chi tiết BusStation tại bến", description = "Lấy thông tin chi tiết về các BusStation tại một bến xe bao gồm thông tin xe, ghi chú, trạng thái", responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Danh sách BusStation chi tiết", summary = "Response mẫu với thông tin chi tiết", value = """
                                        {
                                          "success": true,
                                          "message": "Lấy thông tin chi tiết BusStation thành công",
                                          "data": [
                                            {
                                              "id": 1,
                                              "bus": {
                                                "id": 3,
                                                "name": "Xe Limousine VIP",
                                                "licensePlate": "30A-12345",
                                                "capacity": 22,
                                                "busType": "GIUONG_NAM",
                                                "status": "ACTIVE",
                                                "companyId": 1,
                                                "companyName": "Nhà xe Phương Trang",
                                                "descriptions": "Xe limousine cao cấp với ghế massage",
                                                "createdAt": "2026-02-03T10:30:00",
                                                "updatedAt": "2026-02-03T10:30:00"
                                              },
                                              "stationId": 1,
                                              "stationName": "Bến xe Miền Đông",
                                              "isActive": true,
                                              "notes": "Xe chính tuyến Hà Nội - Hồ Chí Minh, hoạt động 24/7",
                                              "createdAt": "2026-02-03T11:00:00",
                                              "updatedAt": "2026-02-03T11:00:00"
                                            },
                                            {
                                              "id": 2,
                                              "bus": {
                                                "id": 4,
                                                "name": "Xe Thường",
                                                "licensePlate": "30A-67890",
                                                "capacity": 45,
                                                "busType": "GHE_NGOI",
                                                "status": "MAINTENANCE",
                                                "companyId": 1,
                                                "companyName": "Nhà xe Phương Trang",
                                                "descriptions": "Xe ghế ngồi tiêu chuẩn",
                                                "createdAt": "2026-02-03T09:00:00",
                                                "updatedAt": "2026-02-03T09:00:00"
                                              },
                                              "stationId": 1,
                                              "stationName": "Bến xe Miền Đông",
                                              "isActive": false,
                                              "notes": "Xe đang bảo trì định kỳ, tạm ngưng hoạt động",
                                              "createdAt": "2026-02-03T11:15:00",
                                              "updatedAt": "2026-02-03T11:15:00"
                                            }
                                          ]
                                        }
                                        """)))
        })
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<List<BusStationResponse>>> getBusStationDetails(
                        @PathVariable Integer stationId,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] GET /api/bus-company/stations/{}/bus-stations - Get detailed BusStation info",
                                stationId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        List<BusStationResponse> busStations = stationService.getBusStationDetailsForCompany(stationId);

                        log.info("✅ [BUS COMPANY] 200 OK - Retrieved {} detailed BusStation records for station ID: {}",
                                        busStations.size(),
                                        stationId);
                        return ResponseEntity.ok(ApiResponse.<List<BusStationResponse>>builder()
                                        .success(true)
                                        .message("Lấy thông tin chi tiết BusStation thành công")
                                        .data(busStations)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Station not found: {}", stationId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<List<BusStationResponse>>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy bến xe với ID: " + stationId)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get detailed BusStation info for station: {}",
                                        stationId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<List<BusStationResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy thông tin chi tiết BusStation")
                                                        .build());
                }
        }

        @PostMapping("/assign-buses")
        @Operation(summary = "Gắn xe cho bến (cơ bản)", description = "Gắn danh sách xe vào bến xe theo cách cơ bản")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin gắn xe vào bến cơ bản", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
                        @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Thêm xe mới vào bến", summary = "Thêm xe mới vào bến (không thay thế)", description = "Thêm các xe ID 3, 4, 5 vào bến xe ID 1 mà không thay thế xe hiện có", value = """
                                        {
                                          "stationId": 1,
                                          "busIds": [3, 4, 5],
                                          "replaceAll": false
                                        }
                                        """),
                        @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Thay thế tất cả xe tại bến", summary = "Thay thế tất cả xe tại bến", description = "Thay thế tất cả xe hiện có tại bến xe ID 1 bằng xe ID 2, 6", value = """
                                        {
                                          "stationId": 1,
                                          "busIds": [2, 6],
                                          "replaceAll": true
                                        }
                                        """),
                        @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Thêm một xe duy nhất", summary = "Thêm một xe duy nhất vào bến", description = "Thêm xe ID 7 vào bến xe ID 2", value = """
                                        {
                                          "stationId": 2,
                                          "busIds": [7],
                                          "replaceAll": false
                                        }
                                        """)
        }))
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<StationResponse>> assignBusesToStation(
                        @Valid @RequestBody AssignBusToStationRequest request,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] POST /api/bus-company/stations/assign-buses - Assign buses to station");
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        StationResponse station = stationService.assignBusesToStation(request);

                        log.info("✅ [BUS COMPANY] 200 OK - Assigned {} buses to station: {}",
                                        request.getBusIds().size(), station.getName());
                        return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                                        .success(true)
                                        .message("Gắn xe vào bến thành công")
                                        .data(station)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Station or buses not found: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid assign request: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to assign buses to station", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi gắn xe vào bến")
                                                        .build());
                }
        }

        @DeleteMapping("/{stationId}/buses")
        @Operation(summary = "Gỡ xe khỏi bến", description = "Gỡ danh sách xe khỏi bến xe")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<StationResponse>> removeBusesFromStation(
                        @PathVariable Integer stationId,
                        @RequestBody List<Integer> busIds,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] DELETE /api/bus-company/stations/{}/buses - Remove buses from station",
                                stationId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        StationResponse station = stationService.removeBusesFromStation(stationId, busIds);

                        log.info("✅ [BUS COMPANY] 200 OK - Removed {} buses from station: {}", busIds.size(),
                                        station.getName());
                        return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                                        .success(true)
                                        .message("Gỡ xe khỏi bến thành công")
                                        .data(station)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Station not found: {}", stationId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy bến xe với ID: " + stationId)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to remove buses from station: {}",
                                        stationId,
                                        e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<StationResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi gỡ xe khỏi bến")
                                                        .build());
                }
        }

        @GetMapping("/{stationId}/buses")
        @Operation(summary = "Danh sách xe tại bến", description = "Lấy danh sách tất cả xe tại bến (bao gồm xe không hoạt động)")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<List<BusResponse>>> getBusesAtStation(
                        @PathVariable Integer stationId,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] GET /api/bus-company/stations/{}/buses - Get buses at station", stationId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        List<BusResponse> buses = stationService.getBusesAtStationForCompany(stationId);

                        log.info("✅ [BUS COMPANY] 200 OK - Retrieved {} buses at station ID: {}", buses.size(),
                                        stationId);
                        return ResponseEntity.ok(ApiResponse.<List<BusResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách xe tại bến thành công")
                                        .data(buses)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Station not found: {}", stationId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<List<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy bến xe với ID: " + stationId)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get buses at station: {}",
                                        stationId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<List<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách xe tại bến")
                                                        .build());
                }
        }

        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm bến xe", description = "Tìm kiếm bến xe theo từ khóa và địa điểm")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Page<StationResponse>>> searchStations(
                        @RequestParam String keyword,
                        @RequestParam(required = false) String location,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "name") String sortBy,
                        @RequestParam(defaultValue = "asc") String sortDirection,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] GET /api/bus-company/stations/search - Search stations with keyword: {}",
                                keyword);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        if (keyword == null || keyword.trim().isEmpty()) {
                                log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Empty search keyword");
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.<Page<StationResponse>>builder()
                                                                .success(false)
                                                                .message("Từ khóa tìm kiếm không được để trống")
                                                                .build());
                        }

                        StationSearchRequest request = new StationSearchRequest();
                        request.setKeyword(keyword);
                        request.setLocation(location);
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<StationResponse> stations = stationService.getAllStationsForCompany(request);

                        log.info("✅ [BUS COMPANY] 200 OK - Found {} stations matching keyword: {}",
                                        stations.getTotalElements(),
                                        keyword);
                        return ResponseEntity.ok(ApiResponse.<Page<StationResponse>>builder()
                                        .success(true)
                                        .message("Tìm kiếm bến xe thành công")
                                        .data(stations)
                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<StationResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to search stations with keyword: {}",
                                        keyword, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<StationResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tìm kiếm bến xe")
                                                        .build());
                }
        }

        @GetMapping("/{stationId}/buses/search")
        @Operation(summary = "Tìm kiếm xe trong bến", description = "Tìm kiếm xe theo từ khóa tại một bến xe cụ thể")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<List<BusResponse>>> searchBusesAtStation(
                        @PathVariable Integer stationId,
                        @RequestParam String keyword,
                        Authentication authentication) {

                log.info(
                                "🚌 [BUS COMPANY] GET /api/bus-company/stations/{}/buses/search - Search buses at station with keyword: {}",
                                stationId, keyword);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        List<BusResponse> buses = stationService.searchBusesAtStationForCompany(stationId, keyword);

                        log.info("✅ [BUS COMPANY] 200 OK - Found {} buses at station {} matching keyword: {}",
                                        buses.size(), stationId, keyword);
                        return ResponseEntity.ok(ApiResponse.<List<BusResponse>>builder()
                                        .success(true)
                                        .message("Tìm kiếm xe tại bến thành công")
                                        .data(buses)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Station not found: {}", stationId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<List<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy bến xe với ID: " + stationId)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to search buses at station: {}",
                                        stationId,
                                        e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<List<BusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tìm kiếm xe tại bến")
                                                        .build());
                }
        }

        @DeleteMapping("/{stationId}")
        @Operation(summary = "Xóa bến xe", description = "Xóa bến xe (xóa cứng hoặc mềm)")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<Void>> deleteStation(
                        @PathVariable Integer stationId,
                        @RequestBody(required = false) DeleteStationRequest request,
                        Authentication authentication) {

                log.info("🚌 [BUS COMPANY] DELETE /api/bus-company/stations/{} - Delete station", stationId);
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        if (request == null) {
                                request = new DeleteStationRequest();
                        }

                        stationService.deleteStation(stationId, request.isHardDelete());

                        String message = request.isHardDelete()
                                        ? "Xóa bến xe vĩnh viễn thành công"
                                        : "Xóa bến xe thành công";

                        log.info("✅ [BUS COMPANY] 200 OK - Station {} successfully: {}",
                                        request.isHardDelete() ? "hard deleted" : "soft deleted", stationId);

                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message(message)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Station not found for deletion: {}", stationId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy bến xe với ID: " + stationId)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid delete request for station {}: {}",
                                        stationId,
                                        e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Yêu cầu xóa không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to delete station: {}",
                                        stationId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi xóa bến xe")
                                                        .build());
                }
        }

        @GetMapping("/debug/bus-station-data")
        @Operation(summary = "Debug BusStation data", description = "Debug endpoint to check all BusStation records")
        @PreAuthorize("hasRole('BUS_COMPANY')")
        public ResponseEntity<ApiResponse<String>> debugBusStationData(Authentication authentication) {

                log.info("🚌 [BUS COMPANY] GET /api/bus-company/stations/debug/bus-station-data - Debug BusStation data");
                log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                                authentication.getName(), authentication.getAuthorities());

                try {
                        stationService.debugBusStationData();

                        return ResponseEntity.ok(ApiResponse.<String>builder()
                                        .success(true)
                                        .message("Debug completed - check logs")
                                        .data("BusStation debug executed")
                                        .build());

                } catch (Exception e) {
                        log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to debug BusStation data", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<String>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi debug BusStation data")
                                                        .build());
                }
        }
}