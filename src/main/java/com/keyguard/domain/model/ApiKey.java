package com.keyguard.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API Key 的領域模型（Domain Model）。
 * 以 Java Record 實作，確保不可變性（immutable）。
 */
public record ApiKey(
        Long id,
        String vendorName, // 廠商名稱
        String keyHash, // SHA-256 雜湊後的金鑰（不儲存原始金鑰）
        List<String> allowIps, // IP 白名單
        KeyStatus status, // 金鑰狀態（ACTIVE / REVOKED）
        String createdUser, // 建立者
        LocalDateTime createdAt, // 建立時間
        String updatedUser, // 最後更新者
        LocalDateTime updatedAt // 最後更新時間
) {

    /**
     * 判斷請求 IP 是否有權限使用此 API Key。
     * 條件：金鑰狀態為 ACTIVE，且請求 IP 在白名單內（白名單為空則放行所有 IP）。
     */
    public boolean isAccessibleFrom(String requestIp) {
        // 金鑰已撤銷，拒絕存取
        if (status != KeyStatus.ACTIVE) {
            return false;
        }

        // 白名單為空，允許所有 IP
        if (allowIps == null || allowIps.isEmpty()) {
            return true;
        }

        // 比對請求 IP 是否在白名單中
        String normalizedRequestIp = normalizeIp(requestIp);
        return allowIps.stream()
                .map(ApiKey::normalizeIp)
                .anyMatch(normalizedRequestIp::equals);
    }

    /**
     * 正規化 IP 字串，去除首尾空白，null 則回傳空字串。
     */
    private static String normalizeIp(String ip) {
        return ip == null ? "" : ip.trim();
    }
}