package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Integer> {
    Optional<PasswordReset> findByEmailAndOtp(String email, String otp);

    @Modifying
    @Query("DELETE FROM PasswordReset pr WHERE pr.email = :email")
    void deleteByEmail(String email);

    @Modifying
    @Query("DELETE FROM PasswordReset pr WHERE pr.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}