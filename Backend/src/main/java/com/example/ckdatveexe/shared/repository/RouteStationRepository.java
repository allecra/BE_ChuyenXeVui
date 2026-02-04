package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.RouteStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteStationRepository extends JpaRepository<RouteStation, Integer> {

    // Find all stations for a route ordered by index
    List<RouteStation> findByRouteIdOrderByOrderIndex(Integer routeId);

    // Find specific station in route
    Optional<RouteStation> findByRouteIdAndStationId(Integer routeId, Integer stationId);

    // Find by route and order index
    Optional<RouteStation> findByRouteIdAndOrderIndex(Integer routeId, Integer orderIndex);

    // Check if station exists in route
    boolean existsByRouteIdAndStationId(Integer routeId, Integer stationId);

    // Check if order index exists in route
    boolean existsByRouteIdAndOrderIndex(Integer routeId, Integer orderIndex);

    // Get maximum order index for a route
    @Query("SELECT COALESCE(MAX(rs.orderIndex), -1) FROM RouteStation rs WHERE rs.route.id = :routeId")
    Integer findMaxOrderIndexByRouteId(@Param("routeId") Integer routeId);

    // Get minimum order index for a route
    @Query("SELECT COALESCE(MIN(rs.orderIndex), 0) FROM RouteStation rs WHERE rs.route.id = :routeId")
    Integer findMinOrderIndexByRouteId(@Param("routeId") Integer routeId);

    // Count stations in route
    long countByRouteId(Integer routeId);

    // Find routes that contain both departure and arrival stations
    @Query("""
            SELECT rs1.route.id FROM RouteStation rs1, RouteStation rs2
            WHERE rs1.route.id = rs2.route.id
            AND rs1.station.id = :departureStationId
            AND rs2.station.id = :arrivalStationId
            AND rs1.orderIndex < rs2.orderIndex
            """)
    List<Integer> findRouteIdsByStationPair(@Param("departureStationId") Integer departureStationId,
            @Param("arrivalStationId") Integer arrivalStationId);

    // Calculate total distance between two stations in a route
    @Query("""
            SELECT COALESCE(SUM(rs.distanceFromPrevious), 0)
            FROM RouteStation rs
            WHERE rs.route.id = :routeId
            AND rs.orderIndex > :fromOrderIndex
            AND rs.orderIndex <= :toOrderIndex
            """)
    Integer calculateDistanceBetweenStations(@Param("routeId") Integer routeId,
            @Param("fromOrderIndex") Integer fromOrderIndex,
            @Param("toOrderIndex") Integer toOrderIndex);

    // Calculate total price between two stations in a route
    @Query("""
            SELECT COALESCE(SUM(rs.priceFromPrevious), 0.0)
            FROM RouteStation rs
            WHERE rs.route.id = :routeId
            AND rs.orderIndex > :fromOrderIndex
            AND rs.orderIndex <= :toOrderIndex
            """)
    Double calculatePriceBetweenStations(@Param("routeId") Integer routeId,
            @Param("fromOrderIndex") Integer fromOrderIndex,
            @Param("toOrderIndex") Integer toOrderIndex);

    // Get stations between two order indexes (inclusive)
    @Query("""
            SELECT rs FROM RouteStation rs
            WHERE rs.route.id = :routeId
            AND rs.orderIndex >= :fromOrderIndex
            AND rs.orderIndex <= :toOrderIndex
            ORDER BY rs.orderIndex
            """)
    List<RouteStation> findStationsBetweenIndexes(@Param("routeId") Integer routeId,
            @Param("fromOrderIndex") Integer fromOrderIndex,
            @Param("toOrderIndex") Integer toOrderIndex);

    // Find routes by station
    @Query("SELECT rs FROM RouteStation rs WHERE rs.station.id = :stationId ORDER BY rs.route.id, rs.orderIndex")
    List<RouteStation> findByStationId(@Param("stationId") Integer stationId);

    // Delete all stations for a route
    void deleteByRouteId(Integer routeId);

    // Check if route has active schedules (for validation before modification)
    @Query("""
            SELECT COUNT(s) > 0 FROM Schedule s
            WHERE s.route.id = :routeId
            AND s.status != 'CANCELLED'
            """)
    boolean hasActiveSchedules(@Param("routeId") Integer routeId);

    // Check if route has sold tickets (for validation before modification)
    @Query("""
            SELECT COUNT(t) > 0 FROM Ticket t
            JOIN t.schedule s
            WHERE s.route.id = :routeId
            AND t.status != 'CANCELLED'
            """)
    boolean hasSoldTickets(@Param("routeId") Integer routeId);
}