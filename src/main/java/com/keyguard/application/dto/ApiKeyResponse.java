package com.keyguard.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 核發 API Key 的回應 DTO。
 * rawKey 僅於此次回傳，調用方需自行安善保存。
 */
public record ApiKeyResponse(
                Long id,
                String vendorName, // 廠商名稱
                String rawKey, // 原始金鑰（僅顯示一次）
                String keyHash, // SHA-256 雜湊後的金鑰
                List<String> allowIps, // IP 白名單
                String status, // 金鑰狀態
                String createdUser, // 建立者
                LocalDateTime createdAt // 建立時間
) {
}