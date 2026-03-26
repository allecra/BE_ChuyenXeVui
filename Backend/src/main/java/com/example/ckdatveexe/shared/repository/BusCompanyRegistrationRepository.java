package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.BusCompanyRegistration;
import com.example.ckdatveexe.shared.entity.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusCompanyRegistrationRepository extends JpaRepository<BusCompanyRegistration, Integer> {

        @Query("SELECT bcr FROM BusCompanyRegistration bcr WHERE " +
                        "(:status IS NULL OR bcr.status = :status) AND " +
                        "(:companyName IS NULL OR LOWER(bcr.companyName) LIKE LOWER(CONCAT('%', :companyName, '%')))")
        Page<BusCompanyRegistration> findByFilters(@Param("status") RegistrationStatus status,
                        @Param("companyName") String companyName,
                        Pageable pageable);

        @Query("SELECT bcr FROM BusCompanyRegistration bcr WHERE " +
                        "(:status IS NULL OR bcr.status = :status) AND " +
                        "(:searchTerm IS NULL OR " +
                        "LOWER(bcr.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "CAST(bcr.id AS string) LIKE CONCAT('%', :searchTerm, '%'))")
        Page<BusCompanyRegistration> searchByIdOrName(@Param("status") RegistrationStatus status,
                        @Param("searchTerm") String searchTerm,
                        Pageable pageable);

        Optional<BusCompanyRegistration> findByEmailIgnoreCase(String email);

        boolean existsByEmailIgnoreCase(String email);

        boolean existsByCompanyNameIgnoreCase(String companyName);

        // Tìm theo email
        Optional<BusCompanyRegistration> findByEmail(String email);

        // Kiểm tra email tồn tại
        boolean existsByEmail(String email);
}