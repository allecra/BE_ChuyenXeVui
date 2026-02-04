package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Bus;
import com.example.ckdatveexe.shared.entity.BusStation;
import com.example.ckdatveexe.shared.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusStationRepository extends JpaRepository<BusStation, Integer> {

    // Find by bus and station
    Optional<BusStation> findByBusAndStation(Bus bus, Station station);

    Optional<BusStation> findByBusIdAndStationId(Integer busId, Integer stationId);

    // Find all buses at a station
    @Query("SELECT bs FROM BusStation bs WHERE bs.station.id = :stationId AND bs.isActive = true")
    List<BusStation> findActiveByStationId(@Param("stationId") Integer stationId);

    @Query("SELECT bs FROM BusStation bs WHERE bs.station.id = :stationId")
    List<BusStation> findAllByStationId(@Param("stationId") Integer stationId);

    // Find all stations for a bus
    @Query("SELECT bs FROM BusStation bs WHERE bs.bus.id = :busId AND bs.isActive = true")
    List<BusStation> findActiveByBusId(@Param("busId") Integer busId);

    @Query("SELECT bs FROM BusStation bs WHERE bs.bus.id = :busId")
    List<BusStation> findAllByBusId(@Param("busId") Integer busId);

    // Get buses at station (with bus details)
    @Query("SELECT bs.bus FROM BusStation bs WHERE bs.station.id = :stationId AND bs.isActive = true")
    List<Bus> findBusesByStationId(@Param("stationId") Integer stationId);

    // Get stations for bus (with station details)
    @Query("SELECT bs.station FROM BusStation bs WHERE bs.bus.id = :busId AND bs.isActive = true")
    List<Station> findStationsByBusId(@Param("busId") Integer busId);

    // Count buses at station
    @Query("SELECT COUNT(bs) FROM BusStation bs WHERE bs.station.id = :stationId AND bs.isActive = true")
    long countActiveBusesByStationId(@Param("stationId") Integer stationId);

    // Count active buses at station (with bus status)
    @Query("SELECT COUNT(bs) FROM BusStation bs WHERE bs.station.id = :stationId AND bs.isActive = true AND bs.bus.status = 'ACTIVE'")
    long countActiveBusesWithActiveStatusByStationId(@Param("stationId") Integer stationId);

    // Check if bus is at station
    @Query("SELECT COUNT(bs) > 0 FROM BusStation bs WHERE bs.bus.id = :busId AND bs.station.id = :stationId AND bs.isActive = true")
    boolean existsByBusIdAndStationIdAndIsActiveTrue(@Param("busId") Integer busId,
            @Param("stationId") Integer stationId);

    // Deactivate relationship
    @Modifying
    @Query("UPDATE BusStation bs SET bs.isActive = false WHERE bs.bus.id = :busId AND bs.station.id = :stationId")
    int deactivateByBusIdAndStationId(@Param("busId") Integer busId, @Param("stationId") Integer stationId);

    // Deactivate all buses at station
    @Modifying
    @Query("UPDATE BusStation bs SET bs.isActive = false WHERE bs.station.id = :stationId")
    int deactivateAllByStationId(@Param("stationId") Integer stationId);

    // Deactivate all stations for bus
    @Modifying
    @Query("UPDATE BusStation bs SET bs.isActive = false WHERE bs.bus.id = :busId")
    int deactivateAllByBusId(@Param("busId") Integer busId);

    // Delete by bus and station (hard delete)
    void deleteByBusIdAndStationId(Integer busId, Integer stationId);

    // Delete all by station (hard delete)
    void deleteByStationId(Integer stationId);

    // Delete all by bus (hard delete)
    void deleteByBusId(Integer busId);
}