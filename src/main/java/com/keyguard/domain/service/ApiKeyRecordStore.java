package com.keyguard.domain.service;

import com.keyguard.domain.model.ApiKey;

import java.util.List;
import java.util.Optional;

/**
 * API Key 持久化操作的領域服務介面（Port）。
 * 實作類由基礎設施層提供，領域層不直接依賴資料庫模組。
 */
public interface ApiKeyRecordStore {

    /**
     * 建立一筆新的 API Key 記錄（狀態為 ACTIVE）。
     */
    ApiKey issue(String vendorName, String keyHash, List<String> allowIps, String createdUser);

    /**
     * 依雜湊字串查找付活中的 API Key。
     */
    Optional<ApiKey> findActiveByHash(String keyHash);

    /**
     * 撤銷指定 ID 的 API Key，回傳更新後的領域物件。
     * 若 ID 不存在則回傳空。
     */
    Optional<ApiKey> revoke(Long id, String updatedUser);
}