package com.keyguard.application.usecase;

import com.keyguard.application.dto.ApiKeyResponse;
import com.keyguard.application.dto.CreateApiKeyRequest;
import com.keyguard.domain.model.ApiKey;
import com.keyguard.domain.service.ApiKeyCryptographyService;
import com.keyguard.domain.service.ApiKeyRecordStore;
import com.keyguard.infrastructure.config.KeyGuardProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KeyIssuanceService {

    private final ApiKeyRecordStore apiKeyRecordStore;
    private final ApiKeyCryptographyService cryptographyService;
    private final KeyGuardProperties keyGuardProperties;

    public KeyIssuanceService(
            ApiKeyRecordStore apiKeyRecordStore,
            ApiKeyCryptographyService cryptographyService,
            KeyGuardProperties keyGuardProperties
    ) {
        this.apiKeyRecordStore = apiKeyRecordStore;
        this.cryptographyService = cryptographyService;
        this.keyGuardProperties = keyGuardProperties;
    }

    @Transactional
    public ApiKeyResponse generate(CreateApiKeyRequest request) {
        String prefix = resolvePrefix(request.prefix());
        List<String> allowIps = request.allowIps() == null ? List.of() : request.allowIps();
        String rawKey = cryptographyService.generateRawKey(prefix);
        String keyHash = cryptographyService.hash(rawKey);

        ApiKey apiKey = apiKeyRecordStore.issue(
                request.vendorName().trim(),
                keyHash,
                allowIps,
                request.createdUser().trim()
        );

        return new ApiKeyResponse(
                apiKey.id(),
                apiKey.vendorName(),
                rawKey,
                apiKey.keyHash(),
                apiKey.allowIps(),
                apiKey.status().name().toLowerCase(),
                apiKey.createdUser(),
                apiKey.createdAt()
        );
    }

    private String resolvePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return keyGuardProperties.prefix();
        }
        return prefix.trim();
    }
}