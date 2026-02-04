package com.example.ckdatveexe.module.route.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.route.dto.*;
import com.example.ckdatveexe.shared.entity.Route;
import com.example.ckdatveexe.shared.entity.RouteStation;
import com.example.ckdatveexe.shared.entity.Station;
import com.example.ckdatveexe.shared.repository.RouteRepository;
import com.example.ckdatveexe.shared.repository.RouteStationRepository;
import com.example.ckdatveexe.shared.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteStationService {

    private final RouteStationRepository routeStationRepository;
    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;

    /**
     * Add station to route
     */
    @Transactional
    public RouteStationResponse addStationToRoute(Integer routeId, RouteStationRequest request, Integer busCompanyId) {
        log.info("🚌 [ROUTE STATION] Adding station {} to route {} at order {}",
                request.getStationId(), routeId, request.getOrderIndex());

        // Validate route ownership
        Route route = validateRouteOwnership(routeId, busCompanyId);

        // Validate no active schedules or sold tickets
        validateRouteModification(routeId);

        // Validate station exists
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + request.getStationId()));

        // Validate station not already in route
        if (routeStationRepository.existsByRouteIdAndStationId(routeId, request.getStationId())) {
            throw new IllegalArgumentException("Bến xe đã tồn tại trong tuyến đường");
        }

        // Validate order index not already used
        if (routeStationRepository.existsByRouteIdAndOrderIndex(routeId, request.getOrderIndex())) {
            throw new IllegalArgumentException("Thứ tự bến đã được sử dụng: " + request.getOrderIndex());
        }

        // Validate first station (order 0) has distance and price = 0
        if (request.getOrderIndex() == 0) {
            if (request.getDistanceFromPrevious() != 0 || request.getPriceFromPrevious() != 0.0) {
                throw new IllegalArgumentException("Bến đầu tiên phải có khoảng cách và giá từ bến trước = 0");
            }
        }

        // Create route station
        RouteStation routeStation = new RouteStation();
        routeStation.setRoute(route);
        routeStation.setStation(station);
        routeStation.setOrderIndex(request.getOrderIndex());
        routeStation.setDistanceFromPrevious(request.getDistanceFromPrevious());
        routeStation.setPriceFromPrevious(request.getPriceFromPrevious());
        routeStation.setNotes(request.getNotes());

        RouteStation savedRouteStation = routeStationRepository.save(routeStation);
        log.info("✅ [ROUTE STATION] Added station {} to route {} successfully",
                station.getName(), route.getRouteName());

        return convertToResponse(savedRouteStation);
    }

    /**
     * Get all stations for a route
     */
    public List<RouteStationResponse> getRouteStations(Integer routeId, Integer busCompanyId) {
        log.info("🚌 [ROUTE STATION] Getting stations for route {}", routeId);

        // Validate route ownership
        validateRouteOwnership(routeId, busCompanyId);

        List<RouteStation> routeStations = routeStationRepository.findByRouteIdOrderByOrderIndex(routeId);
        Integer maxOrderIndex = routeStations.isEmpty() ? 0
                : routeStations.get(routeStations.size() - 1).getOrderIndex();

        return routeStations.stream()
                .map(rs -> convertToResponseWithCumulative(rs, routeStations, maxOrderIndex))
                .collect(Collectors.toList());
    }

    /**
     * Update station in route
     */
    @Transactional
    public RouteStationResponse updateRouteStation(Integer routeId, Integer stationId,
            RouteStationUpdateRequest request, Integer busCompanyId) {
        log.info("🚌 [ROUTE STATION] Updating station {} in route {}", stationId, routeId);

        // Validate route ownership
        validateRouteOwnership(routeId, busCompanyId);

        // Validate no active schedules or sold tickets
        validateRouteModification(routeId);

        // Find route station
        RouteStation routeStation = routeStationRepository.findByRouteIdAndStationId(routeId, stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến trong tuyến đường"));

        // Update fields if provided
        if (request.getOrderIndex() != null) {
            // Validate new order index not already used by another station
            Optional<RouteStation> existingWithOrder = routeStationRepository
                    .findByRouteIdAndOrderIndex(routeId, request.getOrderIndex());
            if (existingWithOrder.isPresent() && !existingWithOrder.get().getId().equals(routeStation.getId())) {
                throw new IllegalArgumentException("Thứ tự bến đã được sử dụng: " + request.getOrderIndex());
            }

            // Validate first station constraints
            if (request.getOrderIndex() == 0) {
                if ((request.getDistanceFromPrevious() != null && request.getDistanceFromPrevious() != 0) ||
                        (request.getPriceFromPrevious() != null && request.getPriceFromPrevious() != 0.0)) {
                    throw new IllegalArgumentException("Bến đầu tiên phải có khoảng cách và giá từ bến trước = 0");
                }
            }

            routeStation.setOrderIndex(request.getOrderIndex());
        }

        if (request.getDistanceFromPrevious() != null) {
            // Validate first station distance
            if (routeStation.getOrderIndex() == 0 && request.getDistanceFromPrevious() != 0) {
                throw new IllegalArgumentException("Bến đầu tiên phải có khoảng cách từ bến trước = 0");
            }
            routeStation.setDistanceFromPrevious(request.getDistanceFromPrevious());
        }

        if (request.getPriceFromPrevious() != null) {
            // Validate first station price
            if (routeStation.getOrderIndex() == 0 && request.getPriceFromPrevious() != 0.0) {
                throw new IllegalArgumentException("Bến đầu tiên phải có giá từ bến trước = 0");
            }
            routeStation.setPriceFromPrevious(request.getPriceFromPrevious());
        }

        if (request.getNotes() != null) {
            routeStation.setNotes(request.getNotes());
        }

        RouteStation updatedRouteStation = routeStationRepository.save(routeStation);
        log.info("✅ [ROUTE STATION] Updated station {} in route {} successfully",
                routeStation.getStation().getName(), routeStation.getRoute().getRouteName());

        return convertToResponse(updatedRouteStation);
    }

    /**
     * Remove station from route
     */
    @Transactional
    public void removeStationFromRoute(Integer routeId, Integer stationId, Integer busCompanyId) {
        log.info("🚌 [ROUTE STATION] Removing station {} from route {}", stationId, routeId);

        // Validate route ownership
        validateRouteOwnership(routeId, busCompanyId);

        // Validate no active schedules or sold tickets
        validateRouteModification(routeId);

        // Find route station
        RouteStation routeStation = routeStationRepository.findByRouteIdAndStationId(routeId, stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến trong tuyến đường"));

        // Validate not removing first or last station if there are other stations
        long stationCount = routeStationRepository.countByRouteId(routeId);
        if (stationCount > 1) {
            Integer minOrder = routeStationRepository.findMinOrderIndexByRouteId(routeId);
            Integer maxOrder = routeStationRepository.findMaxOrderIndexByRouteId(routeId);

            if (routeStation.getOrderIndex().equals(minOrder) || routeStation.getOrderIndex().equals(maxOrder)) {
                throw new IllegalArgumentException("Không thể xóa bến đầu hoặc bến cuối khi còn bến trung gian");
            }
        }

        routeStationRepository.delete(routeStation);
        log.info("✅ [ROUTE STATION] Removed station {} from route {} successfully",
                routeStation.getStation().getName(), routeStation.getRoute().getRouteName());
    }

    /**
     * Calculate segment price between two stations
     */
    public SegmentPriceResponse calculateSegmentPrice(SegmentPriceRequest request) {
        log.info("🚌 [ROUTE STATION] Calculating price from station {} to {} on route {}",
                request.getDepartureStationId(), request.getArrivalStationId(), request.getRouteId());

        // Validate route exists
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tuyến đường với ID: " + request.getRouteId()));

        // Find departure station in route
        RouteStation departureRouteStation = routeStationRepository
                .findByRouteIdAndStationId(request.getRouteId(), request.getDepartureStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Bến xuất phát không thuộc tuyến đường này"));

        // Find arrival station in route
        RouteStation arrivalRouteStation = routeStationRepository
                .findByRouteIdAndStationId(request.getRouteId(), request.getArrivalStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Bến đến không thuộc tuyến đường này"));

        // Validate order: departure < arrival
        if (departureRouteStation.getOrderIndex() >= arrivalRouteStation.getOrderIndex()) {
            throw new IllegalArgumentException("Bến xuất phát phải đứng trước bến đến trong tuyến");
        }

        // Calculate total distance and price
        Integer totalDistance = routeStationRepository.calculateDistanceBetweenStations(
                request.getRouteId(), departureRouteStation.getOrderIndex(), arrivalRouteStation.getOrderIndex());

        Double totalPrice = routeStationRepository.calculatePriceBetweenStations(
                request.getRouteId(), departureRouteStation.getOrderIndex(), arrivalRouteStation.getOrderIndex());

        // Get stations between departure and arrival (inclusive)
        List<RouteStation> stationsOnSegment = routeStationRepository.findStationsBetweenIndexes(
                request.getRouteId(), departureRouteStation.getOrderIndex(), arrivalRouteStation.getOrderIndex());

        // Build response
        SegmentPriceResponse response = new SegmentPriceResponse();
        response.setRouteId(route.getId());
        response.setRouteName(route.getRouteName());
        response.setDepartureStationId(departureRouteStation.getStation().getId());
        response.setDepartureStationName(departureRouteStation.getStation().getName());
        response.setArrivalStationId(arrivalRouteStation.getStation().getId());
        response.setArrivalStationName(arrivalRouteStation.getStation().getName());
        response.setDepartureOrderIndex(departureRouteStation.getOrderIndex());
        response.setArrivalOrderIndex(arrivalRouteStation.getOrderIndex());
        response.setTotalDistance(totalDistance);
        response.setTotalPrice(totalPrice);
        response.setIntermediateStationCount(stationsOnSegment.size() - 2); // Exclude departure and arrival

        // Convert stations to response
        List<RouteStationResponse> stationResponses = stationsOnSegment.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        response.setStations(stationResponses);

        // Build segment details
        List<SegmentPriceResponse.SegmentDetail> segments = new ArrayList<>();
        for (int i = 1; i < stationsOnSegment.size(); i++) {
            RouteStation current = stationsOnSegment.get(i);
            RouteStation previous = stationsOnSegment.get(i - 1);

            SegmentPriceResponse.SegmentDetail segment = new SegmentPriceResponse.SegmentDetail();
            segment.setFromStation(previous.getStation().getName());
            segment.setToStation(current.getStation().getName());
            segment.setDistance(current.getDistanceFromPrevious());
            segment.setPrice(current.getPriceFromPrevious());
            segments.add(segment);
        }
        response.setSegments(segments);

        log.info("✅ [ROUTE STATION] Calculated segment price: {}km, {}VND", totalDistance, totalPrice);
        return response;
    }

    /**
     * Find routes that contain both departure and arrival stations
     */
    public List<Integer> findRoutesByStationPair(Integer departureStationId, Integer arrivalStationId) {
        return routeStationRepository.findRouteIdsByStationPair(departureStationId, arrivalStationId);
    }

    // ==================== HELPER METHODS ====================

    private Route validateRouteOwnership(Integer routeId, Integer busCompanyId) {
        Route route = routeRepository.findByIdAndBusCompanyId(routeId, busCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + routeId));

        if (route.getStatus() == com.example.ckdatveexe.shared.entity.RouteStatus.DELETED) {
            throw new IllegalArgumentException("Không thể thao tác với tuyến đã bị xóa");
        }

        return route;
    }

    private void validateRouteModification(Integer routeId) {
        if (routeStationRepository.hasActiveSchedules(routeId)) {
            throw new IllegalArgumentException("Không thể thay đổi cấu trúc tuyến khi có lịch chạy đang hoạt động");
        }

        if (routeStationRepository.hasSoldTickets(routeId)) {
            throw new IllegalArgumentException("Không thể thay đổi cấu trúc tuyến khi đã có vé được bán");
        }
    }

    private RouteStationResponse convertToResponse(RouteStation routeStation) {
        RouteStationResponse response = new RouteStationResponse();
        response.setId(routeStation.getId());
        response.setRouteId(routeStation.getRoute().getId());
        response.setRouteName(routeStation.getRoute().getRouteName());
        response.setStationId(routeStation.getStation().getId());
        response.setStationName(routeStation.getStation().getName());
        response.setStationLocation(routeStation.getStation().getLocation());
        response.setOrderIndex(routeStation.getOrderIndex());
        response.setDistanceFromPrevious(routeStation.getDistanceFromPrevious());
        response.setPriceFromPrevious(routeStation.getPriceFromPrevious());
        response.setNotes(routeStation.getNotes());
        response.setCreatedAt(routeStation.getCreatedAt());
        response.setUpdatedAt(routeStation.getUpdatedAt());
        return response;
    }

    private RouteStationResponse convertToResponseWithCumulative(RouteStation routeStation,
            List<RouteStation> allStations,
            Integer maxOrderIndex) {
        RouteStationResponse response = convertToResponse(routeStation);

        // Calculate cumulative distance and price
        int cumulativeDistance = 0;
        double cumulativePrice = 0.0;

        for (RouteStation rs : allStations) {
            if (rs.getOrderIndex() <= routeStation.getOrderIndex()) {
                cumulativeDistance += rs.getDistanceFromPrevious();
                cumulativePrice += rs.getPriceFromPrevious();
            }
        }

        response.setCumulativeDistance(cumulativeDistance);
        response.setCumulativePrice(cumulativePrice);
        response.setStationType(routeStation.getOrderIndex(), maxOrderIndex);

        return response;
    }
}