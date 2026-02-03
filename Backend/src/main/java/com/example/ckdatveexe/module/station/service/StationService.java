package com.example.ckdatveexe.module.station.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.station.dto.*;
import com.example.ckdatveexe.shared.entity.Bus;
import com.example.ckdatveexe.shared.entity.BusStation;
import com.example.ckdatveexe.shared.entity.BusStatus;
import com.example.ckdatveexe.shared.entity.Station;
import com.example.ckdatveexe.shared.repository.BusRepository;
import com.example.ckdatveexe.shared.repository.BusStationRepository;
import com.example.ckdatveexe.shared.repository.StationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationService {

    private final StationRepository stationRepository;
    private final BusRepository busRepository;
    private final BusStationRepository busStationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== USER METHODS ====================

    /**
     * Get all stations for users (with basic info and statistics)
     */
    public Page<StationResponse> getAllStationsForUser(StationSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Station> stations = stationRepository.searchStations(
                request.getKeyword(),
                request.getLocation(),
                pageable);

        return stations.map(this::convertToUserResponse);
    }

    /**
     * Get station detail for user (with buses list)
     */
    public StationResponse getStationDetailForUser(Integer stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        return convertToDetailResponse(station, false); // false = user view (exclude some bus details)
    }

    /**
     * Get buses at station for user
     */
    public List<BusResponse> getBusesAtStationForUser(Integer stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        // Get buses using BusStationRepository
        List<Bus> buses = busStationRepository.findBusesByStationId(stationId);

        return buses.stream()
                .filter(bus -> bus.getStatus() == BusStatus.ACTIVE) // Only active buses for users
                .map(this::convertToBusResponseForUser)
                .collect(Collectors.toList());
    }

    /**
     * Search buses at station for user
     */
    public List<BusResponse> searchBusesAtStationForUser(Integer stationId, String keyword) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        // Get buses using BusStationRepository
        List<Bus> buses = busStationRepository.findBusesByStationId(stationId);

        return buses.stream()
                .filter(bus -> bus.getStatus() == BusStatus.ACTIVE)
                .filter(bus -> keyword == null || keyword.trim().isEmpty() ||
                        bus.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        bus.getLicensePlate().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::convertToBusResponseForUser)
                .collect(Collectors.toList());
    }

    // ==================== BUS_COMPANY METHODS ====================

    /**
     * Create new station (BUS_COMPANY only)
     */
    @Transactional
    public StationResponse createStation(StationCreateRequest request) {
        // Check if station name already exists
        if (stationRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Tên bến xe đã tồn tại: " + request.getName());
        }

        Station station = new Station();
        station.setName(request.getName());
        station.setImage(request.getImage());
        station.setWallpaper(request.getWallpaper());
        station.setDescriptions(request.getDescriptions());
        station.setLocation(request.getLocation());

        Station savedStation = stationRepository.save(station);
        log.info("Created new station: {} at location: {}", savedStation.getName(), savedStation.getLocation());

        return convertToCompanyResponse(savedStation);
    }

    /**
     * Get all stations for bus company (with full details)
     */
    public Page<StationResponse> getAllStationsForCompany(StationSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Station> stations = stationRepository.searchStations(
                request.getKeyword(),
                request.getLocation(),
                pageable);

        return stations.map(this::convertToCompanyResponse);
    }

    /**
     * Get station detail for bus company
     */
    public StationResponse getStationDetailForCompany(Integer stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        return convertToDetailResponse(station, true); // true = company view (full details)
    }

    /**
     * Update station information
     */
    @Transactional
    public StationResponse updateStation(Integer stationId, StationUpdateRequest request) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        // Update fields if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            if (!station.getName().equals(request.getName()) &&
                    stationRepository.existsByNameAndIdNot(request.getName(), stationId)) {
                throw new IllegalArgumentException("Tên bến xe đã tồn tại: " + request.getName());
            }
            station.setName(request.getName());
        }

        if (request.getImage() != null) {
            station.setImage(request.getImage());
        }

        if (request.getWallpaper() != null) {
            station.setWallpaper(request.getWallpaper());
        }

        if (request.getDescriptions() != null) {
            station.setDescriptions(request.getDescriptions());
        }

        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
            station.setLocation(request.getLocation());
        }

        Station updatedStation = stationRepository.save(station);
        log.info("Updated station: {} with ID: {}", updatedStation.getName(), stationId);

        return convertToCompanyResponse(updatedStation);
    }

    /**
     * Assign buses to station
     */
    @Transactional
    public StationResponse assignBusesToStation(AssignBusToStationRequest request) {
        log.info("🏢 [STATION] Assigning buses to station - StationID: {}, BusIDs: {}, ReplaceAll: {}",
                request.getStationId(), request.getBusIds(), request.isReplaceAll());

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + request.getStationId()));

        // Get buses to assign
        List<Bus> busesToAssign = busRepository.findAllById(request.getBusIds());
        log.info("🔍 [STATION] Found {} buses to assign out of {} requested", busesToAssign.size(),
                request.getBusIds().size());

        if (busesToAssign.size() != request.getBusIds().size()) {
            throw new IllegalArgumentException("Một số xe không tồn tại trong hệ thống");
        }

        if (request.isReplaceAll()) {
            // Replace all buses - deactivate current relationships
            List<BusStation> currentBusStations = busStationRepository.findActiveByStationId(request.getStationId());
            log.info("🔍 [STATION] Found {} current active bus-station relationships for station {}",
                    currentBusStations.size(), request.getStationId());

            for (BusStation busStation : currentBusStations) {
                busStation.setIsActive(false);
                log.debug("🔍 [STATION] Deactivating BusStation ID: {}", busStation.getId());
            }
            if (!currentBusStations.isEmpty()) {
                busStationRepository.saveAll(currentBusStations);
                log.info("✅ [STATION] Deactivated {} existing bus-station relationships", currentBusStations.size());
            }

            // Create new relationships
            List<BusStation> newBusStations = new ArrayList<>();
            for (Bus bus : busesToAssign) {
                // Check if relationship already exists (inactive)
                Optional<BusStation> existingBusStation = busStationRepository.findByBusIdAndStationId(
                        bus.getId(), station.getId());

                if (existingBusStation.isPresent()) {
                    // Reactivate existing relationship
                    BusStation busStation = existingBusStation.get();
                    busStation.setIsActive(true);
                    newBusStations.add(busStation);
                    log.info("🔄 [STATION] Reactivated existing relationship: Bus {} - Station {}", bus.getId(),
                            station.getId());
                } else {
                    // Create new relationship
                    BusStation busStation = new BusStation(bus, station);
                    newBusStations.add(busStation);
                    log.info("🆕 [STATION] Created new relationship: Bus {} - Station {}", bus.getId(),
                            station.getId());
                }
            }
            List<BusStation> savedBusStations = busStationRepository.saveAll(newBusStations);
            log.info("✅ [STATION] Saved {} BusStation records. Replaced all buses at station: {} with {} buses",
                    savedBusStations.size(), station.getName(), busesToAssign.size());
        } else {
            // Add new buses - create or activate relationships
            List<BusStation> busStationsToSave = new ArrayList<>();
            for (Bus bus : busesToAssign) {
                // Check if relationship already exists
                Optional<BusStation> existingBusStation = busStationRepository.findByBusIdAndStationId(
                        bus.getId(), station.getId());

                if (existingBusStation.isPresent()) {
                    BusStation busStation = existingBusStation.get();
                    if (!busStation.getIsActive()) {
                        // Reactivate existing relationship
                        busStation.setIsActive(true);
                        busStationsToSave.add(busStation);
                        log.info("🔄 [STATION] Reactivated relationship: Bus {} - Station {}", bus.getId(),
                                station.getId());
                    } else {
                        log.info("ℹ️ [STATION] Relationship already active: Bus {} - Station {}", bus.getId(),
                                station.getId());
                    }
                } else {
                    // Create new relationship
                    BusStation busStation = new BusStation(bus, station);
                    busStationsToSave.add(busStation);
                    log.info("🆕 [STATION] Created new relationship: Bus {} - Station {}", bus.getId(),
                            station.getId());
                }
            }

            if (!busStationsToSave.isEmpty()) {
                List<BusStation> savedBusStations = busStationRepository.saveAll(busStationsToSave);
                log.info("✅ [STATION] Saved {} new BusStation records", savedBusStations.size());
            }
            log.info("✅ [STATION] Added {} buses to station: {}", busesToAssign.size(), station.getName());
        }

        // Verify the assignment worked
        List<BusStation> finalBusStations = busStationRepository.findActiveByStationId(request.getStationId());
        log.info("🔍 [STATION] Final verification: {} active BusStation records for station {}",
                finalBusStations.size(), request.getStationId());

        // Refresh station with buses
        Station updatedStation = stationRepository.findById(request.getStationId()).orElse(station);
        return convertToDetailResponse(updatedStation, true);
    }

    /**
     * Remove buses from station
     */
    @Transactional
    public StationResponse removeBusesFromStation(Integer stationId, List<Integer> busIds) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        // Deactivate bus-station relationships
        List<BusStation> busStationsToDeactivate = new ArrayList<>();
        for (Integer busId : busIds) {
            Optional<BusStation> busStation = busStationRepository.findByBusIdAndStationId(busId, stationId);
            if (busStation.isPresent() && busStation.get().getIsActive()) {
                BusStation bs = busStation.get();
                bs.setIsActive(false);
                busStationsToDeactivate.add(bs);
                log.debug("Deactivated relationship: Bus {} - Station {}", busId, stationId);
            }
        }

        if (!busStationsToDeactivate.isEmpty()) {
            busStationRepository.saveAll(busStationsToDeactivate);
        }

        log.info("Removed {} buses from station: {}", busIds.size(), station.getName());

        // Return updated station response
        return convertToDetailResponse(station, true);
    }

    /**
     * Get buses at station for company (with full details)
     */
    public List<BusResponse> getBusesAtStationForCompany(Integer stationId) {
        log.info("🏢 [STATION] Getting buses at station {} for company", stationId);

        // Verify station exists
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        // Debug: Check BusStation records
        List<BusStation> busStations = busStationRepository.findActiveByStationId(stationId);
        log.info("🔍 [STATION] Found {} active BusStation records for station {}", busStations.size(), stationId);

        for (BusStation bs : busStations) {
            log.info("🔍 [STATION] BusStation ID: {}, Bus ID: {}, Station ID: {}, Active: {}",
                    bs.getId(), bs.getBus().getId(), bs.getStation().getId(), bs.getIsActive());
        }

        // Get buses using BusStationRepository
        List<Bus> buses = busStationRepository.findBusesByStationId(stationId);
        log.info("🔍 [STATION] Found {} buses using findBusesByStationId for station {}", buses.size(), stationId);

        List<BusResponse> result = buses.stream()
                .map(this::convertToBusResponseForCompany)
                .collect(Collectors.toList());

        log.info("🏢 [STATION] 200 OK ✅ - Retrieved {} buses at station ID: {}", result.size(), stationId);
        return result;
    }

    /**
     * Search buses at station for company
     */
    public List<BusResponse> searchBusesAtStationForCompany(Integer stationId, String keyword) {
        // Verify station exists
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        // Get buses using BusStationRepository
        List<Bus> buses = busStationRepository.findBusesByStationId(stationId);

        return buses.stream()
                .filter(bus -> keyword == null || keyword.trim().isEmpty() ||
                        bus.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        bus.getLicensePlate().toLowerCase().contains(keyword.toLowerCase()) ||
                        bus.getCompany().getCompanyName().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::convertToBusResponseForCompany)
                .collect(Collectors.toList());
    }

    /**
     * Delete station (soft or hard delete)
     */
    @Transactional
    public void deleteStation(Integer stationId, boolean hardDelete) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        if (hardDelete) {
            // Hard delete - remove all bus-station relationships first
            busStationRepository.deleteByStationId(stationId);
            stationRepository.delete(station);
            log.info("Hard deleted station: {} with ID: {}", station.getName(), stationId);
        } else {
            // Soft delete - deactivate all bus-station relationships
            busStationRepository.deactivateAllByStationId(stationId);
            log.info("Soft deleted station: {} with ID: {}", station.getName(), stationId);
        }
    }

    /**
     * Assign buses to station with detailed information
     */
    @Transactional
    public StationResponse assignBusesToStationDetailed(BusStationDetailRequest request) {
        log.info(
                "🏢 [STATION] Assigning buses to station with details - StationID: {}, BusStations: {}, ReplaceAll: {}",
                request.getStationId(), request.getBusStations().size(), request.isReplaceAll());

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + request.getStationId()));

        // Validate all buses exist
        List<Integer> busIds = request.getBusStations().stream()
                .map(BusStationCreateRequest::getBusId)
                .collect(Collectors.toList());

        List<Bus> busesToAssign = busRepository.findAllById(busIds);
        log.info("🔍 [STATION] Found {} buses to assign out of {} requested", busesToAssign.size(), busIds.size());

        if (busesToAssign.size() != busIds.size()) {
            throw new IllegalArgumentException("Một số xe không tồn tại trong hệ thống");
        }

        if (request.isReplaceAll()) {
            // Replace all buses - deactivate current relationships
            List<BusStation> currentBusStations = busStationRepository.findActiveByStationId(request.getStationId());
            log.info("🔍 [STATION] Found {} current active bus-station relationships for station {}",
                    currentBusStations.size(), request.getStationId());

            for (BusStation busStation : currentBusStations) {
                busStation.setIsActive(false);
                log.debug("🔍 [STATION] Deactivating BusStation ID: {}", busStation.getId());
            }
            if (!currentBusStations.isEmpty()) {
                busStationRepository.saveAll(currentBusStations);
                log.info("✅ [STATION] Deactivated {} existing bus-station relationships", currentBusStations.size());
            }

            // Create new relationships with detailed info
            List<BusStation> newBusStations = new ArrayList<>();
            for (BusStationCreateRequest busStationReq : request.getBusStations()) {
                Bus bus = busesToAssign.stream()
                        .filter(b -> b.getId().equals(busStationReq.getBusId()))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException("Bus không tồn tại: " + busStationReq.getBusId()));

                // Check if relationship already exists (inactive)
                Optional<BusStation> existingBusStation = busStationRepository.findByBusIdAndStationId(
                        bus.getId(), station.getId());

                if (existingBusStation.isPresent()) {
                    // Reactivate existing relationship with new details
                    BusStation busStation = existingBusStation.get();
                    busStation.setIsActive(busStationReq.getIsActive() != null ? busStationReq.getIsActive() : true);
                    busStation.setNotes(busStationReq.getNotes());
                    newBusStations.add(busStation);
                    log.info("🔄 [STATION] Reactivated existing relationship with details: Bus {} - Station {}",
                            bus.getId(), station.getId());
                } else {
                    // Create new relationship with details
                    BusStation busStation = new BusStation(bus, station, busStationReq.getNotes());
                    busStation.setIsActive(busStationReq.getIsActive() != null ? busStationReq.getIsActive() : true);
                    newBusStations.add(busStation);
                    log.info("🆕 [STATION] Created new relationship with details: Bus {} - Station {}", bus.getId(),
                            station.getId());
                }
            }
            List<BusStation> savedBusStations = busStationRepository.saveAll(newBusStations);
            log.info(
                    "✅ [STATION] Saved {} BusStation records with details. Replaced all buses at station: {} with {} buses",
                    savedBusStations.size(), station.getName(), busesToAssign.size());
        } else {
            // Add new buses with detailed info - create or activate relationships
            List<BusStation> busStationsToSave = new ArrayList<>();
            for (BusStationCreateRequest busStationReq : request.getBusStations()) {
                Bus bus = busesToAssign.stream()
                        .filter(b -> b.getId().equals(busStationReq.getBusId()))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException("Bus không tồn tại: " + busStationReq.getBusId()));

                // Check if relationship already exists
                Optional<BusStation> existingBusStation = busStationRepository.findByBusIdAndStationId(
                        bus.getId(), station.getId());

                if (existingBusStation.isPresent()) {
                    BusStation busStation = existingBusStation.get();
                    if (!busStation.getIsActive()) {
                        // Reactivate existing relationship with new details
                        busStation
                                .setIsActive(busStationReq.getIsActive() != null ? busStationReq.getIsActive() : true);
                        busStation.setNotes(busStationReq.getNotes());
                        busStationsToSave.add(busStation);
                        log.info("🔄 [STATION] Reactivated relationship with details: Bus {} - Station {}", bus.getId(),
                                station.getId());
                    } else {
                        // Update existing active relationship
                        busStation.setNotes(busStationReq.getNotes());
                        busStationsToSave.add(busStation);
                        log.info("🔄 [STATION] Updated existing active relationship: Bus {} - Station {}", bus.getId(),
                                station.getId());
                    }
                } else {
                    // Create new relationship with details
                    BusStation busStation = new BusStation(bus, station, busStationReq.getNotes());
                    busStation.setIsActive(busStationReq.getIsActive() != null ? busStationReq.getIsActive() : true);
                    busStationsToSave.add(busStation);
                    log.info("🆕 [STATION] Created new relationship with details: Bus {} - Station {}", bus.getId(),
                            station.getId());
                }
            }

            if (!busStationsToSave.isEmpty()) {
                List<BusStation> savedBusStations = busStationRepository.saveAll(busStationsToSave);
                log.info("✅ [STATION] Saved {} new BusStation records with details", savedBusStations.size());
            }
            log.info("✅ [STATION] Added {} buses with details to station: {}", busesToAssign.size(), station.getName());
        }

        // Verify the assignment worked
        List<BusStation> finalBusStations = busStationRepository.findActiveByStationId(request.getStationId());
        log.info("🔍 [STATION] Final verification: {} active BusStation records for station {}",
                finalBusStations.size(), request.getStationId());

        // Refresh station with buses
        Station updatedStation = stationRepository.findById(request.getStationId()).orElse(station);
        return convertToDetailResponse(updatedStation, true);
    }

    /**
     * Get detailed BusStation information for a station
     */
    public List<BusStationResponse> getBusStationDetailsForCompany(Integer stationId) {
        log.info("🏢 [STATION] Getting detailed BusStation info for station {}", stationId);

        // Verify station exists
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + stationId));

        // Get BusStation records with details
        List<BusStation> busStations = busStationRepository.findActiveByStationId(stationId);
        log.info("🔍 [STATION] Found {} active BusStation records for station {}", busStations.size(), stationId);

        List<BusStationResponse> result = busStations.stream()
                .map(this::convertToBusStationResponse)
                .collect(Collectors.toList());

        log.info("🏢 [STATION] 200 OK ✅ - Retrieved {} detailed BusStation records for station ID: {}", result.size(),
                stationId);
        return result;
    }

    /**
     * Debug method to check all BusStation records
     */
    public void debugBusStationData() {
        List<BusStation> allBusStations = busStationRepository.findAll();
        log.info("🔍 [DEBUG] Total BusStation records in database: {}", allBusStations.size());

        for (BusStation bs : allBusStations) {
            log.info("🔍 [DEBUG] BusStation ID: {}, Bus ID: {}, Station ID: {}, Active: {}, Created: {}",
                    bs.getId(),
                    bs.getBus() != null ? bs.getBus().getId() : "NULL",
                    bs.getStation() != null ? bs.getStation().getId() : "NULL",
                    bs.getIsActive(),
                    bs.getCreatedAt());
        }
    }

    // ==================== HELPER METHODS ====================

    private Pageable createPageable(StationSearchRequest request) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private StationResponse convertToUserResponse(Station station) {
        long totalBuses = busStationRepository.countActiveBusesByStationId(station.getId());
        long activeBuses = busStationRepository.countActiveBusesWithActiveStatusByStationId(station.getId());

        return StationResponse.forList(
                station.getId(),
                station.getName(),
                station.getImage(),
                station.getLocation(),
                station.getCreatedAt(),
                station.getUpdatedAt(),
                (int) totalBuses,
                (int) activeBuses);
    }

    private StationResponse convertToCompanyResponse(Station station) {
        StationResponse response = new StationResponse();
        response.setId(station.getId());
        response.setName(station.getName());
        response.setImage(station.getImage());
        response.setWallpaper(station.getWallpaper());
        response.setDescriptions(station.getDescriptions());
        response.setLocation(station.getLocation());
        response.setCreatedAt(station.getCreatedAt());
        response.setUpdatedAt(station.getUpdatedAt());

        // Add statistics using BusStationRepository
        long totalBuses = busStationRepository.countActiveBusesByStationId(station.getId());
        long activeBuses = busStationRepository.countActiveBusesWithActiveStatusByStationId(station.getId());
        response.setTotalBuses((int) totalBuses);
        response.setActiveBuses((int) activeBuses);

        return response;
    }

    private StationResponse convertToDetailResponse(Station station, boolean isCompanyView) {
        StationResponse response = convertToCompanyResponse(station);

        // Add buses list using BusStationRepository
        List<Bus> buses = busStationRepository.findBusesByStationId(station.getId());
        List<BusResponse> busResponses = buses.stream()
                .filter(bus -> isCompanyView || bus.getStatus() == BusStatus.ACTIVE)
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

    private BusStationResponse convertToBusStationResponse(BusStation busStation) {
        BusStationResponse response = new BusStationResponse();
        response.setId(busStation.getId());
        response.setBus(convertToBusResponseForCompany(busStation.getBus()));
        response.setStationId(busStation.getStation().getId());
        response.setStationName(busStation.getStation().getName());
        response.setIsActive(busStation.getIsActive());
        response.setNotes(busStation.getNotes());
        response.setCreatedAt(busStation.getCreatedAt());
        response.setUpdatedAt(busStation.getUpdatedAt());
        return response;
    }

    /**
     * Unified method to assign buses to station (supports both basic and detailed
     * modes)
     */
    @Transactional
    public StationResponse assignBusesToStationUnified(UnifiedAssignBusRequest request) {
        log.info("🏢 [STATION] Unified assign buses to station - StationID: {}, Mode: {}, ReplaceAll: {}",
                request.getStationId(),
                request.isBasicMode() ? "BASIC" : "DETAILED",
                request.isReplaceAll());

        // Validation
        if (!request.isValid()) {
            throw new IllegalArgumentException("Phải cung cấp ít nhất một trong hai: busIds hoặc busStations");
        }

        if (request.isBasicMode() && request.isDetailedMode()) {
            throw new IllegalArgumentException(
                    "Chỉ được sử dụng một trong hai mode: basic (busIds) hoặc detailed (busStations)");
        }

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy bến xe với ID: " + request.getStationId()));

        if (request.isBasicMode()) {
            return handleBasicAssignment(request, station);
        } else {
            return handleDetailedAssignment(request, station);
        }
    }

    private StationResponse handleBasicAssignment(UnifiedAssignBusRequest request, Station station) {
        log.info("🔧 [STATION] Processing BASIC assignment for {} buses", request.getBusIds().size());

        // Get buses to assign
        List<Bus> busesToAssign = busRepository.findAllById(request.getBusIds());
        log.info("🔍 [STATION] Found {} buses to assign out of {} requested", busesToAssign.size(),
                request.getBusIds().size());

        if (busesToAssign.size() != request.getBusIds().size()) {
            throw new IllegalArgumentException("Một số xe không tồn tại trong hệ thống");
        }

        if (request.isReplaceAll()) {
            // Replace all buses - deactivate current relationships
            List<BusStation> currentBusStations = busStationRepository.findActiveByStationId(request.getStationId());
            log.info("🔍 [STATION] Found {} current active bus-station relationships for station {}",
                    currentBusStations.size(), request.getStationId());

            for (BusStation busStation : currentBusStations) {
                busStation.setIsActive(false);
            }
            if (!currentBusStations.isEmpty()) {
                busStationRepository.saveAll(currentBusStations);
                log.info("✅ [STATION] Deactivated {} existing bus-station relationships", currentBusStations.size());
            }

            // Create new relationships (basic mode - no notes)
            List<BusStation> newBusStations = new ArrayList<>();
            for (Bus bus : busesToAssign) {
                Optional<BusStation> existingBusStation = busStationRepository.findByBusIdAndStationId(
                        bus.getId(), station.getId());

                if (existingBusStation.isPresent()) {
                    BusStation busStation = existingBusStation.get();
                    busStation.setIsActive(true);
                    newBusStations.add(busStation);
                    log.info("🔄 [STATION] Reactivated existing relationship: Bus {} - Station {}", bus.getId(),
                            station.getId());
                } else {
                    BusStation busStation = new BusStation(bus, station);
                    newBusStations.add(busStation);
                    log.info("🆕 [STATION] Created new relationship: Bus {} - Station {}", bus.getId(),
                            station.getId());
                }
            }
            busStationRepository.saveAll(newBusStations);
            log.info("✅ [STATION] Replaced all buses at station: {} with {} buses", station.getName(),
                    busesToAssign.size());
        } else {
            // Add new buses - create or activate relationships
            List<BusStation> busStationsToSave = new ArrayList<>();
            for (Bus bus : busesToAssign) {
                Optional<BusStation> existingBusStation = busStationRepository.findByBusIdAndStationId(
                        bus.getId(), station.getId());

                if (existingBusStation.isPresent()) {
                    BusStation busStation = existingBusStation.get();
                    if (!busStation.getIsActive()) {
                        busStation.setIsActive(true);
                        busStationsToSave.add(busStation);
                        log.info("🔄 [STATION] Reactivated relationship: Bus {} - Station {}", bus.getId(),
                                station.getId());
                    } else {
                        log.info("ℹ️ [STATION] Relationship already active: Bus {} - Station {}", bus.getId(),
                                station.getId());
                    }
                } else {
                    BusStation busStation = new BusStation(bus, station);
                    busStationsToSave.add(busStation);
                    log.info("🆕 [STATION] Created new relationship: Bus {} - Station {}", bus.getId(),
                            station.getId());
                }
            }

            if (!busStationsToSave.isEmpty()) {
                busStationRepository.saveAll(busStationsToSave);
            }
            log.info("✅ [STATION] Added {} buses to station: {}", busesToAssign.size(), station.getName());
        }

        return refreshAndReturnStation(request.getStationId(), station);
    }

    private StationResponse handleDetailedAssignment(UnifiedAssignBusRequest request, Station station) {
        log.info("🔧 [STATION] Processing DETAILED assignment for {} bus-stations", request.getBusStations().size());

        // Validate all buses exist
        List<Integer> busIds = request.getBusStations().stream()
                .map(BusStationCreateRequest::getBusId)
                .collect(Collectors.toList());

        List<Bus> busesToAssign = busRepository.findAllById(busIds);
        log.info("🔍 [STATION] Found {} buses to assign out of {} requested", busesToAssign.size(), busIds.size());

        if (busesToAssign.size() != busIds.size()) {
            throw new IllegalArgumentException("Một số xe không tồn tại trong hệ thống");
        }

        if (request.isReplaceAll()) {
            // Replace all buses - deactivate current relationships
            List<BusStation> currentBusStations = busStationRepository.findActiveByStationId(request.getStationId());
            log.info("🔍 [STATION] Found {} current active bus-station relationships for station {}",
                    currentBusStations.size(), request.getStationId());

            for (BusStation busStation : currentBusStations) {
                busStation.setIsActive(false);
            }
            if (!currentBusStations.isEmpty()) {
                busStationRepository.saveAll(currentBusStations);
                log.info("✅ [STATION] Deactivated {} existing bus-station relationships", currentBusStations.size());
            }

            // Create new relationships with detailed info
            List<BusStation> newBusStations = new ArrayList<>();
            for (BusStationCreateRequest busStationReq : request.getBusStations()) {
                Bus bus = busesToAssign.stream()
                        .filter(b -> b.getId().equals(busStationReq.getBusId()))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException("Bus không tồn tại: " + busStationReq.getBusId()));

                Optional<BusStation> existingBusStation = busStationRepository.findByBusIdAndStationId(
                        bus.getId(), station.getId());

                if (existingBusStation.isPresent()) {
                    BusStation busStation = existingBusStation.get();
                    busStation.setIsActive(busStationReq.getIsActive() != null ? busStationReq.getIsActive() : true);
                    busStation.setNotes(busStationReq.getNotes());
                    newBusStations.add(busStation);
                    log.info("🔄 [STATION] Reactivated existing relationship with details: Bus {} - Station {}",
                            bus.getId(), station.getId());
                } else {
                    BusStation busStation = new BusStation(bus, station, busStationReq.getNotes());
                    busStation.setIsActive(busStationReq.getIsActive() != null ? busStationReq.getIsActive() : true);
                    newBusStations.add(busStation);
                    log.info("🆕 [STATION] Created new relationship with details: Bus {} - Station {}", bus.getId(),
                            station.getId());
                }
            }
            busStationRepository.saveAll(newBusStations);
            log.info("✅ [STATION] Replaced all buses at station: {} with {} buses (detailed)", station.getName(),
                    busesToAssign.size());
        } else {
            // Add new buses with detailed info
            List<BusStation> busStationsToSave = new ArrayList<>();
            for (BusStationCreateRequest busStationReq : request.getBusStations()) {
                Bus bus = busesToAssign.stream()
                        .filter(b -> b.getId().equals(busStationReq.getBusId()))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException("Bus không tồn tại: " + busStationReq.getBusId()));

                Optional<BusStation> existingBusStation = busStationRepository.findByBusIdAndStationId(
                        bus.getId(), station.getId());

                if (existingBusStation.isPresent()) {
                    BusStation busStation = existingBusStation.get();
                    if (!busStation.getIsActive()) {
                        busStation
                                .setIsActive(busStationReq.getIsActive() != null ? busStationReq.getIsActive() : true);
                        busStation.setNotes(busStationReq.getNotes());
                        busStationsToSave.add(busStation);
                        log.info("🔄 [STATION] Reactivated relationship with details: Bus {} - Station {}", bus.getId(),
                                station.getId());
                    } else {
                        busStation.setNotes(busStationReq.getNotes());
                        busStationsToSave.add(busStation);
                        log.info("🔄 [STATION] Updated existing active relationship: Bus {} - Station {}", bus.getId(),
                                station.getId());
                    }
                } else {
                    BusStation busStation = new BusStation(bus, station, busStationReq.getNotes());
                    busStation.setIsActive(busStationReq.getIsActive() != null ? busStationReq.getIsActive() : true);
                    busStationsToSave.add(busStation);
                    log.info("🆕 [STATION] Created new relationship with details: Bus {} - Station {}", bus.getId(),
                            station.getId());
                }
            }

            if (!busStationsToSave.isEmpty()) {
                busStationRepository.saveAll(busStationsToSave);
            }
            log.info("✅ [STATION] Added {} buses with details to station: {}", busesToAssign.size(), station.getName());
        }

        return refreshAndReturnStation(request.getStationId(), station);
    }

    private StationResponse refreshAndReturnStation(Integer stationId, Station station) {
        // Verify the assignment worked
        List<BusStation> finalBusStations = busStationRepository.findActiveByStationId(stationId);
        log.info("🔍 [STATION] Final verification: {} active BusStation records for station {}",
                finalBusStations.size(), stationId);

        // Refresh station with buses
        Station updatedStation = stationRepository.findById(stationId).orElse(station);
        return convertToDetailResponse(updatedStation, true);
    }
}