package com.example.ckdatveexe.config;

import com.example.ckdatveexe.shared.entity.PaymentProvider;
import com.example.ckdatveexe.shared.entity.ProviderType;
import com.example.ckdatveexe.shared.repository.PaymentProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(10) // Run after other initializers
@Slf4j
public class PaymentDataInitializer implements CommandLineRunner {

    private final PaymentProviderRepository paymentProviderRepository;

    @Override
    public void run(String... args) throws Exception {
        initializePaymentProviders();
    }

    private void initializePaymentProviders() {
        log.info("💳 [INIT] Initializing payment providers...");

        try {
            // Check if providers already exist
            if (paymentProviderRepository.count() > 0) {
                log.info("💳 [INIT] Payment providers already exist, skipping initialization");
                return;
            }

            // Create SYSTEM provider (for internal refunds)
            PaymentProvider systemProvider = new PaymentProvider();
            systemProvider.setProviderName("SYSTEM");
            systemProvider.setProviderType(ProviderType.BANK_TRANSFER);
            systemProvider.setApiEndpoint("internal://system");
            systemProvider.setDescription("Internal System Provider for Refunds");
            systemProvider.setIsActive(true);

            // Create MoMo provider
            PaymentProvider momoProvider = new PaymentProvider();
            momoProvider.setProviderName("MOMO");
            momoProvider.setProviderType(ProviderType.E_WALLET);
            momoProvider.setApiEndpoint("https://test-payment.momo.vn/v2/gateway/api/create");
            momoProvider.setDescription("MoMo E-Wallet Payment Gateway");
            momoProvider.setIsActive(true);

            // Create SePay provider
            PaymentProvider sepayProvider = new PaymentProvider();
            sepayProvider.setProviderName("SEPAY");
            sepayProvider.setProviderType(ProviderType.QR_CODE);
            sepayProvider.setApiEndpoint("https://my.sepay.vn/userapi");
            sepayProvider.setDescription("SePay QR Code Payment Gateway");
            sepayProvider.setIsActive(true);

            // Save providers
            paymentProviderRepository.save(systemProvider);
            paymentProviderRepository.save(momoProvider);
            paymentProviderRepository.save(sepayProvider);

            log.info("✅ [INIT] Payment providers initialized successfully");
            log.info("💳 [INIT] - System provider created");
            log.info("💳 [INIT] - MoMo E-Wallet provider created");
            log.info("💳 [INIT] - SePay QR Code provider created");

        } catch (Exception e) {
            log.error("💥 [INIT] Error initializing payment providers", e);
        }
    }
}