package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Integer> {

    Optional<CancellationPolicy> findByRouteId(Integer routeId);

    @Query("SELECT cp FROM CancellationPolicy cp WHERE cp.route.id = :routeId ORDER BY cp.createdAt DESC")
    Optional<CancellationPolicy> findLatestByRouteId(@Param("routeId") Integer routeId);
}