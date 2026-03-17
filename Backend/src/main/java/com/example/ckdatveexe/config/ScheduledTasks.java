package com.example.ckdatveexe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.ckdatveexe.module.auth.service.AuthService;
import com.example.ckdatveexe.module.payment.service.PaymentService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final AuthService authService;
    private final PaymentService paymentService;

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired tokens");
        try {
            authService.cleanupExpiredTokens();
            log.info("Expired tokens cleanup completed successfully");
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens", e);
        }
    }

    @Scheduled(fixedRate = 1800000) // Run every 30 minutes
    public void cleanupExpiredPayments() {
        log.info("💳 [SCHEDULED] Starting cleanup of expired payments");
        try {
            paymentService.cancelExpiredPayments();
            log.info("✅ [SCHEDULED] Expired payments cleanup completed successfully");
        } catch (Exception e) {
            log.error("💥 [SCHEDULED] Failed to cleanup expired payments", e);
        }
    }
}