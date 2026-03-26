package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.User;
import com.example.ckdatveexe.shared.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.busCompany WHERE u.email = :email AND u.status = :status")
    Optional<User> findByEmailAndStatusWithBusCompany(@Param("email") String email, @Param("status") UserStatus status);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.busCompany.id = :companyId")
    List<User> findByBusCompanyId(@Param("companyId") Integer companyId);

    // ===== ADMIN PAGING =====
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    // ===== ADMIN SEARCH =====
    // ===== ADMIN SEARCH ===== (bỏ filter status <> DELETED)
    @Query("""
                SELECT u FROM User u
                WHERE (
                    LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            """)
    Page<User> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
                SELECT u FROM User u
                WHERE u.status = :status
                AND (
                    LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            """)
    Page<User> searchByKeywordAndStatus(
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            Pageable pageable);

    // ===== USER PROFILE METHODS =====
    boolean existsByEmailAndIdNot(String email, Integer id);

    boolean existsByIdCardAndIdNot(String idCard, Integer id);

    boolean existsByIdCard(String idCard);
}