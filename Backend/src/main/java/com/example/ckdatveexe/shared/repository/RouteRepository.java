package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Route;
import com.example.ckdatveexe.shared.entity.RouteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {

    // Basic queries
    Optional<Route> findByIdAndStatus(Integer id, RouteStatus status);
    
    List<Route> findByStatus(RouteStatus status);
    
    Page<Route> findByStatus(RouteStatus status, Pageable pageable);

    // Company-specific queries
    Page<Route> findByBusCompanyIdAndStatusNot(Integer busCompanyId, RouteStatus status, Pageable pageable);
    
    List<Route> findByBusCompanyIdAndStatusNot(Integer busCompanyId, RouteStatus status);
    
    Optional<Route> findByIdAndBusCompanyId(Integer id, Integer busCompanyId);

    // Check if route name exists for company
    boolean existsByRouteNameAndBusCompanyIdAndStatusNot(String routeName, Integer busCompanyId, RouteStatus status);
    
    boolean existsByRouteNameAndBusCompanyIdAndIdNotAndStatusNot(String routeName, Integer busCompanyId, Integer id, RouteStatus status);

    // Complex search query for users
    @Query("""
        SELECT r FROM Route r 
        WHERE r.status = 'ACTIVE'
        AND (:startLocation IS NULL OR LOWER(r.startLocation) LIKE LOWER(CONCAT('%', :startLocation, '%')))
        AND (:endLocation IS NULL OR LOWER(r.endLocation) LIKE LOWER(CONCAT('%', :endLocation, '%')))
        AND (:minPrice IS NULL OR r.price >= :minPrice)
        AND (:maxPrice IS NULL OR r.price <= :maxPrice)
        AND (:busCompanyId IS NULL OR r.busCompany.id = :busCompanyId)
        AND (:busCompanyName IS NULL OR LOWER(r.busCompany.companyName) LIKE LOWER(CONCAT('%', :busCompanyName, '%')))
        AND EXISTS (
            SELECT s FROM Schedule s 
            WHERE s.route = r 
            AND s.status = 'AVAILABLE'
            AND s.bus.status = 'ACTIVE'
            AND (:departureDate IS NULL OR DATE(s.departureTime) = DATE(:departureDate))
            AND (:departureTimeFrom IS NULL OR TIME(s.departureTime) >= TIME(:departureTimeFrom))
            AND (:departureTimeTo IS NULL OR TIME(s.departureTime) <= TIME(:departureTimeTo))
            AND (:arrivalDate IS NULL OR DATE(s.arrivalTime) = DATE(:arrivalDate))
            AND (:arrivalTimeFrom IS NULL OR TIME(s.arrivalTime) >= TIME(:arrivalTimeFrom))
            AND (:arrivalTimeTo IS NULL OR TIME(s.arrivalTime) <= TIME(:arrivalTimeTo))
        )
        """)
    Page<Route> searchRoutesForUser(
            @Param("startLocation") String startLocation,
            @Param("endLocation") String endLocation,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("busCompanyId") Integer busCompanyId,
            @Param("busCompanyName") String busCompanyName,
            @Param("departureDate") LocalDateTime departureDate,
            @Param("departureTimeFrom") LocalDateTime departureTimeFrom,
            @Param("departureTimeTo") LocalDateTime departureTimeTo,
            @Param("arrivalDate") LocalDateTime arrivalDate,
            @Param("arrivalTimeFrom") LocalDateTime arrivalTimeFrom,
            @Param("arrivalTimeTo") LocalDateTime arrivalTimeTo,
            Pageable pageable);

    // Complex search query for companies
    @Query("""
        SELECT r FROM Route r 
        WHERE r.busCompany.id = :busCompanyId
        AND r.status != 'DELETED'
        AND (:startLocation IS NULL OR LOWER(r.startLocation) LIKE LOWER(CONCAT('%', :startLocation, '%')))
        AND (:endLocation IS NULL OR LOWER(r.endLocation) LIKE LOWER(CONCAT('%', :endLocation, '%')))
        AND (:minPrice IS NULL OR r.price >= :minPrice)
        AND (:maxPrice IS NULL OR r.price <= :maxPrice)
        AND (:status IS NULL OR r.status = :status)
        """)
    Page<Route> searchRoutesForCompany(
            @Param("busCompanyId") Integer busCompanyId,
            @Param("startLocation") String startLocation,
            @Param("endLocation") String endLocation,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("status") RouteStatus status,
            Pageable pageable);

    // Count active routes by company
    long countByBusCompanyIdAndStatus(Integer busCompanyId, RouteStatus status);

    // Get routes with active schedules
    @Query("""
        SELECT DISTINCT r FROM Route r 
        JOIN r.schedules s 
        WHERE r.status = 'ACTIVE' 
        AND s.status = 'AVAILABLE'
        AND s.bus.status = 'ACTIVE'
        """)
    List<Route> findActiveRoutesWithActiveSchedules();

    // Check if route can be hard deleted (no active schedules)
    @Query("""
        SELECT COUNT(s) FROM Schedule s 
        WHERE s.route.id = :routeId 
        AND s.status != 'CANCELLED'
        """)
    long countActiveSchedulesByRouteId(@Param("routeId") Integer routeId);
}