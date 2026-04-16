package com.keyguard.application.usecase;

import com.keyguard.domain.model.ApiKey;
import com.keyguard.domain.service.ApiKeyCryptographyService;
import com.keyguard.domain.service.ApiKeyRecordStore;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * API Key 驗證服務。
 * 接收原始金鑰與請求 IP，驗證金鑰是否有效且 IP 在白名單內。
 */
@Service
public class ApiKeyAuthenticationService {

    private final ApiKeyCryptographyService cryptographyService;
    private final ApiKeyRecordStore apiKeyRecordStore;

    public ApiKeyAuthenticationService(
            ApiKeyCryptographyService cryptographyService,
            ApiKeyRecordStore apiKeyRecordStore) {
        this.cryptographyService = cryptographyService;
        this.apiKeyRecordStore = apiKeyRecordStore;
    }

    /**
     * 驗證原始金鑰與請求 IP。
     * 1. 對原始金鑰進行 SHA-256 雜湊
     * 2. 查詢資料庫是否存在活耶金鑰
     * 3. 檢查請求 IP 是否在白名單內
     * 成功回傳 ApiKey，失敗回傳空。
     */
    public Optional<ApiKey> authenticate(String rawKey, String requestIp) {
        // 金鑰為空時直接拒絕
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }

        // 雜湊後查詢資料庫，確認金鑰付活且 IP 在白名單內
        String hashedKey = cryptographyService.hash(rawKey.trim());
        return apiKeyRecordStore.findActiveByHash(hashedKey)
                .filter(apiKey -> apiKey.isAccessibleFrom(requestIp));
    }
}