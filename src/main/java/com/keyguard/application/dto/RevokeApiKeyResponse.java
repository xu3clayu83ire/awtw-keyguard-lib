package com.keyguard.application.dto;

import java.time.LocalDateTime;

/**
 * 撤銷 API Key 的回應 DTO。
 */
public record RevokeApiKeyResponse(
                Long id,
                String vendorName, // 廠商名稱
                String status, // 金鑰狀態（應為 revoked）
                String updatedUser, // 撤銷操作者
                LocalDateTime updatedAt // 撤銷時間
) {
}