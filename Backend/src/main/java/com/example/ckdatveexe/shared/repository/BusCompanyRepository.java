package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.BusCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusCompanyRepository extends JpaRepository<BusCompany, Integer> {

        @Query("SELECT bc FROM BusCompany bc WHERE " +
                        "(:companyName IS NULL OR LOWER(bc.companyName) LIKE LOWER(CONCAT('%', :companyName, '%')))")
        Page<BusCompany> findByFilters(@Param("companyName") String companyName, Pageable pageable);

        @Query("SELECT bc FROM BusCompany bc WHERE " +
                        "(:searchTerm IS NULL OR " +
                        "LOWER(bc.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "CAST(bc.id AS string) LIKE CONCAT('%', :searchTerm, '%'))")
        Page<BusCompany> searchByIdOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

        Optional<BusCompany> findByCompanyNameIgnoreCase(String companyName);

        boolean existsByCompanyNameIgnoreCase(String companyName);

        // Tìm kiếm theo tên
        Page<BusCompany> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);
}