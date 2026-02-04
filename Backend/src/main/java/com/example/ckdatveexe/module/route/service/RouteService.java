package com.example.ckdatveexe.module.route.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.route.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;
    private final BusCompanyRepository busCompanyRepository;
    private final BusRepository busRepository;

    // ==================== USER METHODS ====================

    /**
     * Get all active routes for users with pagination
     */
    public Page<RouteResponse> getAllActiveRoutesForUser(RouteSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Route> routes = routeRepository.findByStatus(RouteStatus.ACTIVE, pageable);
        return routes.map(this::convertToUserResponse);
    }

    /**
     * Get route detail for user (only active routes)
     */
    public RouteResponse getRouteDetailForUser(Integer routeId) {
        Route route = routeRepository.findByIdAndStatus(routeId, RouteStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + routeId));

        return convertToDetailResponse(route, false); // false = user view
    }

    /**
     * Search routes for users with complex filters
     */
    public Page<RouteResponse> searchRoutesForUser(RouteSearchRequest request) {
        log.info(
                "🔍 [ROUTE] Searching routes for user with filters: startLocation={}, endLocation={}, minPrice={}, maxPrice={}",
                request.getStartLocation(), request.getEndLocation(), request.getMinPrice(), request.getMaxPrice());

        Pageable pageable = createPageable(request);

        Page<Route> routes = routeRepository.searchRoutesForUser(
                request.getStartLocation(),
                request.getEndLocation(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getBusCompanyId(),
                request.getBusCompanyName(),
                request.getDepartureDate(),
                request.getDepartureTimeFrom(),
                request.getDepartureTimeTo(),
                request.getArrivalDate(),
                request.getArrivalTimeFrom(),
                request.getArrivalTimeTo(),
                pageable);

        log.info("✅ [ROUTE] Found {} routes matching user search criteria", routes.getTotalElements());
        return routes.map(this::convertToUserResponse);
    }

    /**
     * Get buses on route for user (only active buses)
     */
    public List<BusResponse> getBusesOnRouteForUser(Integer routeId) {
        Route route = routeRepository.findByIdAndStatus(routeId, RouteStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + routeId));

        // Get active buses that have active schedules on this route
        List<Bus> buses = busRepository.findActiveBusesByRouteId(routeId);

        return buses.stream()
                .map(this::convertToBusResponseForUser)
                .collect(Collectors.toList());
    }

    // ==================== COMPANY METHODS ====================

    /**
     * Create new route (COMPANY only)
     */
    @Transactional
    public RouteResponse createRoute(RouteCreateRequest request, Integer busCompanyId) {
        log.info("🚌 [ROUTE] Creating new route: {} for company ID: {}", request.getRouteName(), busCompanyId);

        // Validate bus company exists
        BusCompany busCompany = busCompanyRepository.findById(busCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + busCompanyId));

        // Check if route name already exists for this company
        if (routeRepository.existsByRouteNameAndBusCompanyIdAndStatusNot(
                request.getRouteName(), busCompanyId, RouteStatus.DELETED)) {
            throw new IllegalArgumentException("Tên tuyến đã tồn tại trong nhà xe: " + request.getRouteName());
        }

        // Validate stations exist
        Station departureStation = stationRepository.findById(request.getDepartureStationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy bến xuất phát với ID: " + request.getDepartureStationId()));

        Station arrivalStation = stationRepository.findById(request.getArrivalStationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy bến đến với ID: " + request.getArrivalStationId()));

        // Validate departure and arrival stations are different
        if (request.getDepartureStationId().equals(request.getArrivalStationId())) {
            throw new IllegalArgumentException("Bến xuất phát và bến đến không được giống nhau");
        }

        // Create route
        Route route = new Route();
        route.setRouteName(request.getRouteName());
        route.setStartLocation(request.getStartLocation());
        route.setEndLocation(request.getEndLocation());
        route.setPrice(request.getPrice());
        route.setDuration(request.getDuration());
        route.setDistance(request.getDistance());
        route.setDescriptions(request.getDescriptions());
        route.setStatus(RouteStatus.ACTIVE);
        route.setBusCompany(busCompany);
        route.setDepartureStation(departureStation);
        route.setArrivalStation(arrivalStation);

        Route savedRoute = routeRepository.save(route);
        log.info("✅ [ROUTE] Created new route: {} with ID: {}", savedRoute.getRouteName(), savedRoute.getId());

        return convertToCompanyResponse(savedRoute);
    }

    /**
     * Get all routes for company (including inactive)
     */
    public Page<RouteResponse> getAllRoutesForCompany(Integer busCompanyId, RouteSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Route> routes = routeRepository.findByBusCompanyIdAndStatusNot(busCompanyId, RouteStatus.DELETED,
                pageable);
        return routes.map(this::convertToCompanyResponse);
    }

    /**
     * Get route detail for company
     */
    public RouteResponse getRouteDetailForCompany(Integer routeId, Integer busCompanyId) {
        Route route = routeRepository.findByIdAndBusCompanyId(routeId, busCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + routeId));

        if (route.getStatus() == RouteStatus.DELETED) {
            throw new ResourceNotFoundException("Tuyến đường đã bị xóa");
        }

        return convertToDetailResponse(route, true); // true = company view
    }

    /**
     * Update route information
     */
    @Transactional
    public RouteResponse updateRoute(Integer routeId, RouteUpdateRequest request, Integer busCompanyId) {
        log.info("🚌 [ROUTE] Updating route ID: {} for company ID: {}", routeId, busCompanyId);

        Route route = routeRepository.findByIdAndBusCompanyId(routeId, busCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + routeId));

        if (route.getStatus() == RouteStatus.DELETED) {
            throw new IllegalArgumentException("Không thể cập nhật tuyến đã bị xóa");
        }

        // Update fields if provided
        if (request.getRouteName() != null && !request.getRouteName().trim().isEmpty()) {
            if (!route.getRouteName().equals(request.getRouteName()) &&
                    routeRepository.existsByRouteNameAndBusCompanyIdAndIdNotAndStatusNot(
                            request.getRouteName(), busCompanyId, routeId, RouteStatus.DELETED)) {
                throw new IllegalArgumentException("Tên tuyến đã tồn tại: " + request.getRouteName());
            }
            route.setRouteName(request.getRouteName());
        }

        if (request.getStartLocation() != null && !request.getStartLocation().trim().isEmpty()) {
            route.setStartLocation(request.getStartLocation());
        }

        if (request.getEndLocation() != null && !request.getEndLocation().trim().isEmpty()) {
            route.setEndLocation(request.getEndLocation());
        }

        if (request.getPrice() != null) {
            route.setPrice(request.getPrice());
        }

        if (request.getDuration() != null) {
            route.setDuration(request.getDuration());
        }

        if (request.getDistance() != null) {
            route.setDistance(request.getDistance());
        }

        if (request.getDescriptions() != null) {
            route.setDescriptions(request.getDescriptions());
        }

        if (request.getStatus() != null) {
            route.setStatus(request.getStatus());
        }

        // Update stations if provided
        if (request.getDepartureStationId() != null) {
            Station departureStation = stationRepository.findById(request.getDepartureStationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy bến xuất phát với ID: " + request.getDepartureStationId()));
            route.setDepartureStation(departureStation);
        }

        if (request.getArrivalStationId() != null) {
            Station arrivalStation = stationRepository.findById(request.getArrivalStationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy bến đến với ID: " + request.getArrivalStationId()));
            route.setArrivalStation(arrivalStation);
        }

        // Validate departure and arrival stations are different
        if (route.getDepartureStation().getId().equals(route.getArrivalStation().getId())) {
            throw new IllegalArgumentException("Bến xuất phát và bến đến không được giống nhau");
        }

        Route updatedRoute = routeRepository.save(route);
        log.info("✅ [ROUTE] Updated route: {} with ID: {}", updatedRoute.getRouteName(), routeId);

        return convertToCompanyResponse(updatedRoute);
    }

    /**
     * Search routes for company
     */
    public Page<RouteResponse> searchRoutesForCompany(Integer busCompanyId, RouteSearchRequest request) {
        log.info("🚌 [ROUTE] Searching routes for company ID: {} with filters", busCompanyId);

        Pageable pageable = createPageable(request);

        Page<Route> routes = routeRepository.searchRoutesForCompany(
                busCompanyId,
                request.getStartLocation(),
                request.getEndLocation(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getStatus(),
                pageable);

        log.info("✅ [ROUTE] Found {} routes for company search", routes.getTotalElements());
        return routes.map(this::convertToCompanyResponse);
    }

    /**
     * Delete route (soft or hard delete)
     */
    @Transactional
    public void deleteRoute(Integer routeId, Integer busCompanyId, DeleteRouteRequest request) {
        log.info("🚌 [ROUTE] Deleting route ID: {} for company ID: {}, hardDelete: {}",
                routeId, busCompanyId, request.isHardDelete());

        Route route = routeRepository.findByIdAndBusCompanyId(routeId, busCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + routeId));

        if (route.getStatus() == RouteStatus.DELETED) {
            throw new IllegalArgumentException("Tuyến đường đã bị xóa trước đó");
        }

        if (request.isHardDelete()) {
            // Check if route has active schedules
            long activeScheduleCount = routeRepository.countActiveSchedulesByRouteId(routeId);
            if (activeScheduleCount > 0) {
                throw new IllegalArgumentException("Không thể xóa vĩnh viễn tuyến có lịch trình đang hoạt động");
            }

            routeRepository.delete(route);
            log.info("✅ [ROUTE] Hard deleted route: {} with ID: {}", route.getRouteName(), routeId);
        } else {
            // Soft delete
            route.setStatus(RouteStatus.DELETED);
            route.setDeletedAt(LocalDateTime.now());
            routeRepository.save(route);
            log.info("✅ [ROUTE] Soft deleted route: {} with ID: {}", route.getRouteName(), routeId);
        }
    }

    /**
     * Get buses on route for company (including inactive buses)
     */
    public List<BusResponse> getBusesOnRouteForCompany(Integer routeId, Integer busCompanyId) {
        Route route = routeRepository.findByIdAndBusCompanyId(routeId, busCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + routeId));

        if (route.getStatus() == RouteStatus.DELETED) {
            throw new ResourceNotFoundException("Tuyến đường đã bị xóa");
        }

        // Get all buses that have schedules on this route
        List<Bus> buses = busRepository.findBusesByRouteId(routeId);

        return buses.stream()
                .map(this::convertToBusResponseForCompany)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private Pageable createPageable(RouteSearchRequest request) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private RouteResponse convertToUserResponse(Route route) {
        // Count active schedules and buses for this route
        long activeScheduleCount = route.getSchedules() != null ? route.getSchedules().stream()
                .filter(s -> s.getStatus() == ScheduleStatus.AVAILABLE)
                .count() : 0;

        long activeBusCount = route.getSchedules() != null ? route.getSchedules().stream()
                .filter(s -> s.getStatus() == ScheduleStatus.AVAILABLE && s.getBus().getStatus() == BusStatus.ACTIVE)
                .map(s -> s.getBus().getId())
                .distinct()
                .count() : 0;

        return RouteResponse.forList(
                route.getId(),
                route.getRouteName(),
                route.getStartLocation(),
                route.getEndLocation(),
                route.getPrice(),
                route.getDuration(),
                route.getDistance(),
                route.getStatus(),
                route.getBusCompany().getId(),
                route.getBusCompany().getCompanyName(),
                route.getDepartureStation().getName(),
                route.getArrivalStation().getName(),
                (int) activeScheduleCount,
                (int) activeBusCount,
                route.getCreatedAt(),
                route.getUpdatedAt());
    }

    private RouteResponse convertToCompanyResponse(Route route) {
        RouteResponse response = new RouteResponse();
        response.setId(route.getId());
        response.setRouteName(route.getRouteName());
        response.setStartLocation(route.getStartLocation());
        response.setEndLocation(route.getEndLocation());
        response.setPrice(route.getPrice());
        response.setDuration(route.getDuration());
        response.setDistance(route.getDistance());
        response.setDescriptions(route.getDescriptions());
        response.setStatus(route.getStatus());
        response.setBusCompanyId(route.getBusCompany().getId());
        response.setBusCompanyName(route.getBusCompany().getCompanyName());
        response.setDepartureStationId(route.getDepartureStation().getId());
        response.setDepartureStationName(route.getDepartureStation().getName());
        response.setArrivalStationId(route.getArrivalStation().getId());
        response.setArrivalStationName(route.getArrivalStation().getName());
        response.setCreatedAt(route.getCreatedAt());
        response.setUpdatedAt(route.getUpdatedAt());
        response.setDeletedAt(route.getDeletedAt());

        // Count schedules and buses
        long activeScheduleCount = route.getSchedules() != null ? route.getSchedules().stream()
                .filter(s -> s.getStatus() != ScheduleStatus.CANCELLED)
                .count() : 0;

        long activeBusCount = route.getSchedules() != null ? route.getSchedules().stream()
                .filter(s -> s.getStatus() != ScheduleStatus.CANCELLED)
                .map(s -> s.getBus().getId())
                .distinct()
                .count() : 0;

        response.setActiveScheduleCount((int) activeScheduleCount);
        response.setActiveBusCount((int) activeBusCount);

        return response;
    }

    private RouteResponse convertToDetailResponse(Route route, boolean isCompanyView) {
        RouteResponse response = isCompanyView ? convertToCompanyResponse(route) : convertToUserResponse(route);

        // Add buses list
        List<Bus> buses = isCompanyView ? busRepository.findBusesByRouteId(route.getId())
                : busRepository.findActiveBusesByRouteId(route.getId());

        List<BusResponse> busResponses = buses.stream()
                .map(bus -> isCompanyView ? convertToBusResponseForCompany(bus) : convertToBusResponseForUser(bus))
                .collect(Collectors.toList());

        response.setBuses(busResponses);
        return response;
    }

    private BusResponse convertToBusResponseForUser(Bus bus) {
        BusResponse response = new BusResponse();
        response.setId(bus.getId());
        response.setName(bus.getName());
        response.setLicensePlate(bus.getLicensePlate());
        response.setCapacity(bus.getCapacity());
        response.setBusType(bus.getBusType());
        response.setStatus(bus.getStatus());
        response.setCompanyId(bus.getCompany().getId());
        response.setCompanyName(bus.getCompany().getCompanyName());
        return response;
    }

    private BusResponse convertToBusResponseForCompany(Bus bus) {
        BusResponse response = convertToBusResponseForUser(bus);
        response.setDescriptions(bus.getDescriptions());
        response.setCreatedAt(bus.getCreatedAt());
        response.setUpdatedAt(bus.getUpdatedAt());
        return response;
    }
}