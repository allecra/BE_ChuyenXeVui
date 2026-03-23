package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Payment;
import com.example.ckdatveexe.shared.entity.PaymentStatus;
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
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Find by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find by provider transaction ID
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    // Find by user
    List<Payment> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // Find by ticket
    List<Payment> findByTicketId(Integer ticketId);

    // Find by status
    List<Payment> findByStatus(PaymentStatus status);

    // Find by status with pagination
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    // Find expired payments
    List<Payment> findByStatusAndExpiredAtBefore(PaymentStatus status, LocalDateTime expiredAt);

    // Find by payment provider
    List<Payment> findByPaymentProviderIdOrderByCreatedAtDesc(Integer paymentProviderId);

    // Find by date range
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate ORDER BY p.createdAt DESC")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find payments by date range with pagination
    Page<Payment> findByCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    List<Payment> findByCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate);

    // Find successful payments by user
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.status = 'COMPLETED' ORDER BY p.paidAt DESC")
    List<Payment> findCompletedPaymentsByUser(@Param("userId") Integer userId);

    // Count payments by status
    long countByStatus(PaymentStatus status);

    // Count payments by provider
    long countByPaymentProviderId(Integer paymentProviderId);

    // Sum amount by status
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    Double sumAmountByStatus(@Param("status") PaymentStatus status);

    // Sum amount by provider
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentProvider.id = :providerId AND p.status = 'COMPLETED'")
    Double sumCompletedAmountByProvider(@Param("providerId") Integer providerId);

    // Find recent payments
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Payment> findRecentPayments(@Param("since") LocalDateTime since);

    // Check if user has pending payment for ticket
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.user.id = :userId AND p.ticket.id = :ticketId AND p.status IN ('PENDING', 'PROCESSING')")
    boolean hasPendingPaymentForTicket(@Param("userId") Integer userId, @Param("ticketId") Integer ticketId);

    // Find payments by status and amount condition
    List<Payment> findByStatusAndAmountLessThan(PaymentStatus status, Double amount);

    // Find by user with pagination
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);
}