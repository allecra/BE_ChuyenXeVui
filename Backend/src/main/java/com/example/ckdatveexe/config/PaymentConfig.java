package com.example.ckdatveexe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@Slf4j
public class PaymentConfig {

    /**
     * RestTemplate bean for payment provider API calls
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("🔧 [CONFIG] Configuring RestTemplate for payment providers");

        return builder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(60))
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }

    /**
     * ClientHttpRequestFactory for RestTemplate
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 30 seconds
        factory.setReadTimeout(60000); // 60 seconds
        return factory;
    }

    /**
     * ObjectMapper bean for JSON serialization/deserialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        log.info("🔧 [CONFIG] Configuring ObjectMapper for payment system");

        ObjectMapper mapper = new ObjectMapper();

        // Register JavaTimeModule for LocalDateTime support
        mapper.registerModule(new JavaTimeModule());

        // Use snake_case for property naming (common in payment APIs)
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // Don't fail on unknown properties
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}