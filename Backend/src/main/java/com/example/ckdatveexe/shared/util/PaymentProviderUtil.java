package com.example.ckdatveexe.shared.util;

import com.example.ckdatveexe.shared.entity.PaymentProvider;
import com.example.ckdatveexe.shared.repository.PaymentProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProviderUtil {

    private final PaymentProviderRepository paymentProviderRepository;

    public PaymentProvider getSystemProvider() {
        return paymentProviderRepository.findByProviderName("SYSTEM")
                .orElseThrow(() -> new RuntimeException("System payment provider not found"));
    }

    public PaymentProvider getProviderByName(String name) {
        return paymentProviderRepository.findByProviderName(name)
                .orElseThrow(() -> new RuntimeException("Payment provider not found: " + name));
    }
}