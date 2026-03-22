package com.example.ckdatveexe.module.payment.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.payment.dto.*;
import com.example.ckdatveexe.module.ticket.service.TicketService;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentManagementService {

        private final PaymentRepository paymentRepository;
        private final TicketRepository ticketRepository;
        private final TicketService ticketService;

        @Transactional
        public PaymentResponse processSepayCallback(SepayCallbackRequest request) {
                log.info("Processing SePay callback: {}", request.getReferenceCode());

                // Find payment by reference code
                Payment payment = paymentRepository.findByTransactionId(request.getReferenceCode())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Payment not found for reference: " + request.getReferenceCode()));

                // Verify amount
                BigDecimal callbackAmount = new BigDecimal(request.getAmountIn());
                BigDecimal paymentAmount = payment.getAmountAsBigDecimal();
                if (callbackAmount.compareTo(paymentAmount) != 0) {
                        log.error("Amount mismatch for payment {}: expected {}, received {}",
                                        payment.getId(), paymentAmount, callbackAmount);
                        throw new IllegalArgumentException("Amount mismatch");
                }

                // Update payment status
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setCompletedAt(LocalDateTime.now());
                payment.setPaymentDetails("SePay callback processed: " + request.getContent());

                Payment savedPayment = paymentRepository.save(payment);

                // Confirm ticket if payment is for a ticket
                if (payment.getTicket() != null) {
                        ticketService.confirmTicketPayment(payment.getTicket().getId());
                }

                log.info("SePay payment completed successfully: {}", payment.getTransactionId());

                return PaymentResponse.fromEntity(savedPayment);
        }

        @Transactional
        public PaymentResponse processRefund(RefundProcessRequest request) {
                Payment originalPayment = paymentRepository.findById(request.getPaymentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

                // Validate refund amount
                BigDecimal originalAmount = originalPayment.getAmountAsBigDecimal();
                if (request.getRefundAmount().compareTo(originalAmount) > 0) {
                        throw new IllegalArgumentException("Refund amount cannot exceed original payment amount");
                }

                // Check if already refunded
                if (originalPayment.getStatus() == PaymentStatus.REFUNDED) {
                        throw new IllegalArgumentException("Payment has already been refunded");
                }

                // Create refund payment record
                Payment refundPayment = new Payment();
                refundPayment.setUser(originalPayment.getUser());
                refundPayment.setTicket(originalPayment.getTicket());
                refundPayment.setAmount(request.getRefundAmount().negate()); // Negative amount for refund
                refundPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
                refundPayment.setPaymentProvider(originalPayment.getPaymentProvider());
                refundPayment.setStatus(PaymentStatus.PENDING);
                refundPayment
                                .setTransactionId("REFUND_" + originalPayment.getTransactionId() + "_"
                                                + System.currentTimeMillis());
                refundPayment.setDescription("Refund for payment: " + originalPayment.getTransactionId() +
                                (request.getRefundReason() != null ? " - " + request.getRefundReason() : ""));

                // Store bank account info for refund
                if (request.getBankAccountNumber() != null) {
                        refundPayment.setPaymentDetails(String.format(
                                        "Bank: %s, Account: %s, Name: %s",
                                        request.getBankName(),
                                        request.getBankAccountNumber(),
                                        request.getBankAccountName()));
                }

                Payment savedRefund = paymentRepository.save(refundPayment);

                // Update original payment status
                originalPayment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(originalPayment);

                log.info("Refund processed: {} for original payment: {}",
                                savedRefund.getTransactionId(), originalPayment.getTransactionId());

                return PaymentResponse.fromEntity(savedRefund);
        }

        public Page<PaymentResponse> getAllPayments(Pageable pageable) {
                return paymentRepository.findAll(pageable)
                                .map(PaymentResponse::fromEntity);
        }

        public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
                return paymentRepository.findByStatus(status, pageable)
                                .map(PaymentResponse::fromEntity);
        }

        public Page<PaymentResponse> getPaymentsByDateRange(LocalDateTime fromDate, LocalDateTime toDate,
                        Pageable pageable) {
                return paymentRepository.findByCreatedAtBetween(fromDate, toDate, pageable)
                                .map(PaymentResponse::fromEntity);
        }

        public PaymentReportResponse generatePaymentReport(LocalDateTime fromDate, LocalDateTime toDate) {
                List<Payment> payments;

                if (fromDate != null && toDate != null) {
                        payments = paymentRepository.findByCreatedAtBetween(fromDate, toDate);
                } else {
                        // Default to last 30 days
                        LocalDateTime defaultFromDate = LocalDateTime.now().minusDays(30);
                        LocalDateTime defaultToDate = LocalDateTime.now();
                        payments = paymentRepository.findByCreatedAtBetween(defaultFromDate, defaultToDate);
                }

                PaymentReportResponse report = new PaymentReportResponse();
                report.setReportDate(LocalDateTime.now());
                report.setTotalPayments(payments.size());

                // Count by status
                Map<PaymentStatus, Long> statusCounts = payments.stream()
                                .collect(Collectors.groupingBy(Payment::getStatus, Collectors.counting()));

                report.setSuccessfulPayments(statusCounts.getOrDefault(PaymentStatus.COMPLETED, 0L).intValue());
                report.setFailedPayments(statusCounts.getOrDefault(PaymentStatus.FAILED, 0L).intValue());
                report.setPendingPayments(statusCounts.getOrDefault(PaymentStatus.PENDING, 0L).intValue());
                report.setRefundedPayments(statusCounts.getOrDefault(PaymentStatus.REFUNDED, 0L).intValue());

                // Amount calculations
                BigDecimal totalAmount = payments.stream()
                                .filter(p -> p.getAmount() > 0) // Exclude refunds (negative amounts)
                                .map(p -> BigDecimal.valueOf(p.getAmount()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                report.setTotalAmount(totalAmount);

                BigDecimal successfulAmount = payments.stream()
                                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getAmount() > 0)
                                .map(p -> BigDecimal.valueOf(p.getAmount()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                report.setSuccessfulAmount(successfulAmount);

                BigDecimal refundedAmount = payments.stream()
                                .filter(p -> p.getAmount() < 0) // Negative amounts are refunds
                                .map(p -> BigDecimal.valueOf(Math.abs(p.getAmount())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                report.setRefundedAmount(refundedAmount);

                report.setNetRevenue(successfulAmount.subtract(refundedAmount));

                // Payments by method
                Map<String, Integer> paymentsByMethod = payments.stream()
                                .filter(p -> p.getAmount() > 0)
                                .collect(Collectors.groupingBy(
                                                p -> p.getPaymentMethod().name(),
                                                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
                report.setPaymentsByMethod(paymentsByMethod);

                // Amount by method
                Map<String, BigDecimal> amountByMethod = new HashMap<>();
                for (PaymentMethod method : PaymentMethod.values()) {
                        BigDecimal amount = payments.stream()
                                        .filter(p -> p.getPaymentMethod() == method && p.getAmount() > 0)
                                        .map(p -> BigDecimal.valueOf(p.getAmount()))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        amountByMethod.put(method.name(), amount);
                }
                report.setAmountByMethod(amountByMethod);

                // Payments by provider
                Map<String, Integer> paymentsByProvider = payments.stream()
                                .filter(p -> p.getAmount() > 0)
                                .collect(Collectors.groupingBy(
                                                p -> p.getPaymentProvider().getProviderName(),
                                                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
                report.setPaymentsByProvider(paymentsByProvider);

                // Amount by provider
                Map<String, BigDecimal> amountByProvider = new HashMap<>();
                List<PaymentProvider> providers = payments.stream()
                                .map(Payment::getPaymentProvider)
                                .distinct()
                                .collect(Collectors.toList());

                for (PaymentProvider provider : providers) {
                        BigDecimal amount = payments.stream()
                                        .filter(p -> p.getPaymentProvider().equals(provider) && p.getAmount() > 0)
                                        .map(p -> BigDecimal.valueOf(p.getAmount()))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        amountByProvider.put(provider.getProviderName(), amount);
                }
                report.setAmountByProvider(amountByProvider);

                // Average payment amount
                long positivePaymentCount = payments.stream()
                                .mapToLong(p -> p.getAmount() > 0 ? 1 : 0)
                                .sum();

                if (positivePaymentCount > 0) {
                        report.setAveragePaymentAmount(
                                        totalAmount.divide(BigDecimal.valueOf(positivePaymentCount), 2,
                                                        RoundingMode.HALF_UP));
                } else {
                        report.setAveragePaymentAmount(BigDecimal.ZERO);
                }

                // Success rate
                if (report.getTotalPayments() > 0) {
                        report.setSuccessRate(
                                        (double) report.getSuccessfulPayments() / report.getTotalPayments() * 100);
                } else {
                        report.setSuccessRate(0.0);
                }

                return report;
        }

        public List<PaymentResponse> getPendingRefunds() {
                return paymentRepository.findByStatusAndAmountLessThan(PaymentStatus.PENDING, 0.0)
                                .stream()
                                .map(PaymentResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        @Transactional
        public void markRefundAsCompleted(Integer refundPaymentId) {
                Payment refundPayment = paymentRepository.findById(refundPaymentId)
                                .orElseThrow(() -> new ResourceNotFoundException("Refund payment not found"));

                if (refundPayment.getAmount() >= 0) {
                        throw new IllegalArgumentException("This is not a refund payment");
                }

                refundPayment.setStatus(PaymentStatus.COMPLETED);
                refundPayment.setCompletedAt(LocalDateTime.now());
                paymentRepository.save(refundPayment);

                log.info("Marked refund as completed: {}", refundPayment.getTransactionId());
        }
}