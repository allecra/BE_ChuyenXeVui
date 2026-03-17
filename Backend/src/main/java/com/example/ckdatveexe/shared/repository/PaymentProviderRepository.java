package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.PaymentProvider;
import com.example.ckdatveexe.shared.entity.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentProviderRepository extends JpaRepository<PaymentProvider, Integer> {

    // Find by provider name
    Optional<PaymentProvider> findByProviderName(String providerName);

    // Find by provider type
    List<PaymentProvider> findByProviderType(ProviderType providerType);

    // Find by provider name (case insensitive)
    Optional<PaymentProvider> findByProviderNameIgnoreCase(String providerName);

    // Check if provider exists
    boolean existsByProviderName(String providerName);
}