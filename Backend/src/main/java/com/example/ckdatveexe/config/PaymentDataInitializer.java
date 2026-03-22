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

            // Create MoMo provider
            PaymentProvider momoProvider = new PaymentProvider();
            momoProvider.setProviderName("MOMO");
            momoProvider.setProviderType(ProviderType.E_WALLET);
            momoProvider.setApiEndpoint("https://test-payment.momo.vn/v2/gateway/api/create");

            // Create SePay provider
            PaymentProvider sepayProvider = new PaymentProvider();
            sepayProvider.setProviderName("SEPAY");
            sepayProvider.setProviderType(ProviderType.QR_CODE);
            sepayProvider.setApiEndpoint("https://my.sepay.vn/userapi");

            // Save providers
            paymentProviderRepository.save(momoProvider);
            paymentProviderRepository.save(sepayProvider);

            log.info("✅ [INIT] Payment providers initialized successfully");
            log.info("💳 [INIT] - MoMo E-Wallet provider created");
            log.info("💳 [INIT] - SePay QR Code provider created");

        } catch (Exception e) {
            log.error("💥 [INIT] Error initializing payment providers", e);
        }
    }
}