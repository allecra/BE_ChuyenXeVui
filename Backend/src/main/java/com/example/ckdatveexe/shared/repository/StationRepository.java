package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Station;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {

        // Basic queries
        Optional<Station> findByName(String name);

        boolean existsByName(String name);

        boolean existsByNameAndIdNot(String name, Integer id);

        // Search queries
        @Query("SELECT s FROM Station s WHERE " +
                        "(:keyword IS NULL OR " +
                        "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:location IS NULL OR LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%')))")
        Page<Station> searchStations(@Param("keyword") String keyword,
                        @Param("location") String location,
                        Pageable pageable);

        // Find stations by location
        @Query("SELECT s FROM Station s WHERE LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))")
        List<Station> findByLocationContainingIgnoreCase(@Param("location") String location);

        // Find stations with buses
        @Query("SELECT DISTINCT s FROM Station s LEFT JOIN FETCH s.buses b WHERE s.id = :stationId")
        Optional<Station> findByIdWithBuses(@Param("stationId") Integer stationId);

        // Count buses at station
        @Query("SELECT COUNT(b) FROM Station s JOIN s.buses b WHERE s.id = :stationId")
        long countBusesByStationId(@Param("stationId") Integer stationId);

        // Count active buses at station
        @Query("SELECT COUNT(b) FROM Station s JOIN s.buses b WHERE s.id = :stationId AND b.status = 'ACTIVE'")
        long countActiveBusesByStationId(@Param("stationId") Integer stationId);

        // Find stations by bus company
        @Query("SELECT DISTINCT s FROM Station s JOIN s.buses b WHERE b.company.id = :companyId")
        List<Station> findStationsByBusCompanyId(@Param("companyId") Integer companyId);

        // Search stations with bus count
        @Query("SELECT s, COUNT(b) as busCount FROM Station s LEFT JOIN s.buses b WHERE " +
                        "(:keyword IS NULL OR " +
                        "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:location IS NULL OR LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
                        "GROUP BY s.id")
        Page<Object[]> searchStationsWithBusCount(@Param("keyword") String keyword,
                        @Param("location") String location,
                        Pageable pageable);
}