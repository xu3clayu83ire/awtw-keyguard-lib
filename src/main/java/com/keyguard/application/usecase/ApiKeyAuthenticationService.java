package com.keyguard.application.usecase;

import com.keyguard.domain.model.ApiKey;
import com.keyguard.domain.service.ApiKeyCryptographyService;
import com.keyguard.domain.service.ApiKeyRecordStore;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApiKeyAuthenticationService {

    private final ApiKeyCryptographyService cryptographyService;
    private final ApiKeyRecordStore apiKeyRecordStore;

    public ApiKeyAuthenticationService(
            ApiKeyCryptographyService cryptographyService,
            ApiKeyRecordStore apiKeyRecordStore
    ) {
        this.cryptographyService = cryptographyService;
        this.apiKeyRecordStore = apiKeyRecordStore;
    }

    public Optional<ApiKey> authenticate(String rawKey, String requestIp) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }

        String hashedKey = cryptographyService.hash(rawKey.trim());
        return apiKeyRecordStore.findActiveByHash(hashedKey)
                .filter(apiKey -> apiKey.isAccessibleFrom(requestIp));
    }
}