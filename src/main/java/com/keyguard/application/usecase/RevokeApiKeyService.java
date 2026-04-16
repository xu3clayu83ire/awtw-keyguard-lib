package com.keyguard.application.usecase;

import com.keyguard.application.dto.RevokeApiKeyResponse;
import com.keyguard.domain.model.ApiKey;
import com.keyguard.domain.service.ApiKeyRecordStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class RevokeApiKeyService {

    private final ApiKeyRecordStore apiKeyRecordStore;

    public RevokeApiKeyService(ApiKeyRecordStore apiKeyRecordStore) {
        this.apiKeyRecordStore = apiKeyRecordStore;
    }

    @Transactional
    public RevokeApiKeyResponse revoke(Long id, String updatedUser) {
        ApiKey apiKey = apiKeyRecordStore.revoke(id, updatedUser.trim())
                .orElseThrow(() -> new NoSuchElementException("API key not found: " + id));

        return new RevokeApiKeyResponse(
                apiKey.id(),
                apiKey.vendorName(),
                apiKey.status().name().toLowerCase(),
                apiKey.updatedUser(),
                apiKey.updatedAt()
        );
    }
}