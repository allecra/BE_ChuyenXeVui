package com.example.ckdatveexe.module.payment.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.payment.dto.PaymentCallbackRequest;
import com.example.ckdatveexe.module.payment.dto.PaymentCreateRequest;
import com.example.ckdatveexe.module.payment.dto.PaymentResponse;
import com.example.ckdatveexe.module.payment.provider.PaymentProviderInterface;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.PaymentProviderRepository;
import com.example.ckdatveexe.shared.repository.PaymentRepository;
import com.example.ckdatveexe.shared.repository.TicketRepository;
import com.example.ckdatveexe.shared.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final List<PaymentProviderInterface> paymentProviders;
    private final ObjectMapper objectMapper;

    private static final AtomicLong transactionCounter = new AtomicLong(1);

    /**
     * Create payment request
     */
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request, Integer userId) {
        log.info("💳 [PAYMENT] Creating payment for user {} with provider {}", userId, request.getProvider());

        try {
            // Validate ticket
            Ticket ticket = ticketRepository.findById(request.getTicketId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy vé với ID: " + request.getTicketId()));

            // Validate user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

            // Check if ticket belongs to user or is available for purchase
            if (ticket.getUser() != null && !ticket.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Vé không thuộc về người dùng này");
            }

            // Find payment provider
            PaymentProvider provider = paymentProviderRepository.findByProviderName(request.getProvider())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy nhà cung cấp thanh toán: " + request.getProvider()));

            // Get payment provider implementation
            PaymentProviderInterface providerImpl = getPaymentProvider(request.getProvider());
            if (!providerImpl.isEnabled()) {
                throw new IllegalArgumentException(
                        "Nhà cung cấp thanh toán " + request.getProvider() + " hiện không khả dụng");
            }

            // Generate unique transaction ID
            String transactionId = generateTransactionId();

            // Create payment record
            Payment payment = new Payment();
            payment.setTransactionId(transactionId);
            payment.setPaymentMethod(PaymentMethod.ONLINE);
            payment.setAmount(java.math.BigDecimal.valueOf(request.getAmount()));
            payment.setCurrency("VND");
            payment.setStatus(PaymentStatus.PENDING);
            payment.setDescription(request.getDescription());
            payment.setPaymentProvider(provider);
            payment.setUser(user);
            payment.setTicket(ticket);
            payment.setExpiredAt(LocalDateTime.now().plusHours(1)); // Default 1 hour expiry

            Payment savedPayment = paymentRepository.save(payment);

            // Call provider to create payment
            PaymentResponse providerResponse = providerImpl.createPayment(request, transactionId);

            // Update payment with provider response
            savedPayment.setQrCodeUrl(providerResponse.getQrCodeUrl());
            savedPayment.setPaymentUrl(providerResponse.getPaymentUrl());
            savedPayment.setProviderTransactionId(providerResponse.getProviderTransactionId());
            if (providerResponse.getExpiredAt() != null) {
                savedPayment.setExpiredAt(providerResponse.getExpiredAt());
            }

            Payment updatedPayment = paymentRepository.save(savedPayment);

            log.info("✅ [PAYMENT] Payment created successfully with transaction ID: {}", transactionId);
            return PaymentResponse.fromEntity(updatedPayment);

        } catch (Exception e) {
            log.error("💥 [PAYMENT] Error creating payment for user {}", userId, e);
            throw new RuntimeException("Không thể tạo thanh toán: " + e.getMessage(), e);
        }
    }

    /**
     * Get payment status
     */
    public PaymentResponse getPaymentStatus(String transactionId) {
        log.info("💳 [PAYMENT] Getting payment status for transaction: {}", transactionId);

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với ID: " + transactionId));

        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Process payment callback
     */
    @Transactional
    public boolean processCallback(String providerName, Object rawCallback) {
        log.info("💳 [PAYMENT] Processing callback from provider: {}", providerName);

        try {
            // Get payment provider implementation
            PaymentProviderInterface providerImpl = getPaymentProvider(providerName);

            // Process callback
            PaymentCallbackRequest callback = providerImpl.processCallback(rawCallback);

            // Verify callback
            if (!providerImpl.verifyCallback(callback)) {
                log.warn("⚠️ [PAYMENT] Invalid callback signature from {}", providerName);
                return false;
            }

            // Find payment
            Payment payment = paymentRepository.findByTransactionId(callback.getTransactionId())
                    .orElse(null);

            if (payment == null) {
                log.warn("⚠️ [PAYMENT] Payment not found for transaction: {}", callback.getTransactionId());
                return false;
            }

            // Update payment status
            PaymentStatus newStatus = mapCallbackStatusToPaymentStatus(callback.getStatus());
            PaymentStatus oldStatus = payment.getStatus();

            payment.setStatus(newStatus);
            payment.setProviderTransactionId(callback.getProviderTransactionId());
            payment.setCallbackData(objectMapper.writeValueAsString(callback.getRawData()));

            if (newStatus == PaymentStatus.COMPLETED) {
                payment.setPaidAt(LocalDateTime.now());

                // Update ticket status if payment is completed
                if (payment.getTicket() != null) {
                    Ticket ticket = payment.getTicket();
                    ticket.setStatus(TicketStatus.CONFIRMED);
                    ticketRepository.save(ticket);
                    log.info("🎫 [PAYMENT] Ticket {} confirmed for payment {}", ticket.getId(),
                            payment.getTransactionId());
                }
            }

            paymentRepository.save(payment);

            log.info("✅ [PAYMENT] Payment status updated from {} to {} for transaction: {}",
                    oldStatus, newStatus, callback.getTransactionId());

            return true;

        } catch (Exception e) {
            log.error("💥 [PAYMENT] Error processing callback from {}", providerName, e);
            return false;
        }
    }

    /**
     * Get user payments
     */
    public List<PaymentResponse> getUserPayments(Integer userId) {
        log.info("💳 [PAYMENT] Getting payments for user: {}", userId);

        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return payments.stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    /**
     * Cancel expired payments
     */
    @Transactional
    public void cancelExpiredPayments() {
        log.info("💳 [PAYMENT] Cancelling expired payments");

        LocalDateTime now = LocalDateTime.now();
        List<Payment> expiredPayments = paymentRepository.findByStatusAndExpiredAtBefore(
                PaymentStatus.PENDING, now);

        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            log.info("⏰ [PAYMENT] Expired payment: {}", payment.getTransactionId());
        }

        log.info("✅ [PAYMENT] Cancelled {} expired payments", expiredPayments.size());
    }

    private PaymentProviderInterface getPaymentProvider(String providerName) {
        return paymentProviders.stream()
                .filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Không hỗ trợ nhà cung cấp thanh toán: " + providerName));
    }

    private String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long counter = transactionCounter.getAndIncrement();
        return String.format("PAY_%s_%03d", timestamp, counter % 1000);
    }

    private PaymentStatus mapCallbackStatusToPaymentStatus(String callbackStatus) {
        return switch (callbackStatus.toUpperCase()) {
            case "COMPLETED", "SUCCESS", "PAID" -> PaymentStatus.COMPLETED;
            case "FAILED", "ERROR" -> PaymentStatus.FAILED;
            case "CANCELLED", "CANCEL" -> PaymentStatus.CANCELLED;
            case "PROCESSING" -> PaymentStatus.PROCESSING;
            default -> PaymentStatus.PENDING;
        };
    }
}