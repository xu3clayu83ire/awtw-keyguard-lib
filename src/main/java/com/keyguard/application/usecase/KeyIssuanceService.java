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

/**
 * API Key 核發服務。
 * 負責產生原始金鑰、進行雜湊並儲存到資料庫。
 */
@Service
public class KeyIssuanceService {

    private final ApiKeyRecordStore apiKeyRecordStore;
    private final ApiKeyCryptographyService cryptographyService;
    private final KeyGuardProperties keyGuardProperties;

    public KeyIssuanceService(
            ApiKeyRecordStore apiKeyRecordStore,
            ApiKeyCryptographyService cryptographyService,
            KeyGuardProperties keyGuardProperties) {
        this.apiKeyRecordStore = apiKeyRecordStore;
        this.cryptographyService = cryptographyService;
        this.keyGuardProperties = keyGuardProperties;
    }

    /**
     * 核發一筆新的 API Key。
     * 流程：解析字首 → 產生原始金鑰 → SHA-256 雜湊 → 儲存資料庫 → 回傳回應（含原始金鑰）。
     */
    @Transactional
    public ApiKeyResponse generate(CreateApiKeyRequest request) {
        // 解析字首：請求有提供則使用，否則使用系統預設
        String prefix = resolvePrefix(request.prefix());
        List<String> allowIps = request.allowIps() == null ? List.of() : request.allowIps();
        // 產生原始金鑰並計算雜湊
        String rawKey = cryptographyService.generateRawKey(prefix);
        String keyHash = cryptographyService.hash(rawKey);

        // 儲存雜湊後的金鑰至資料庫
        ApiKey apiKey = apiKeyRecordStore.issue(
                request.vendorName().trim(),
                keyHash,
                allowIps,
                request.createdUser().trim());

        // 回傳回應（包含原始金鑰，僅此一次）
        return new ApiKeyResponse(
                apiKey.id(),
                apiKey.vendorName(),
                rawKey,
                apiKey.keyHash(),
                apiKey.allowIps(),
                apiKey.status().name().toLowerCase(),
                apiKey.createdUser(),
                apiKey.createdAt());
    }

    /**
     * 解析金鑰字首：請求提供則使用，空白則改用設定檔中的預設字首。
     */
    private String resolvePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return keyGuardProperties.prefix();
        }
        return prefix.trim();
    }
}