package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.User;
import com.example.ckdatveexe.shared.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.busCompany.id = :companyId")
    List<User> findByBusCompanyId(@Param("companyId") Integer companyId);
}