package com.keyguard.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * KeyGuard 應用程式的自訂設定屬性，對應 application.yml 中的 keyguard.* 設定。
 */
@ConfigurationProperties(prefix = "keyguard")
public record KeyGuardProperties(
                String prefix, // API Key 預設字首（如 amsk）
                String protectedPathPrefix, // 需要驗證的路徑前綴（如 /api/v1/todolist）
                String forwardedForHeader, // 獲取真實客戶端 IP 的 Header 名稱（如 X-Forwarded-For）
                String apiKeyHeader // 應用程式讀取 API Key 的 Header 名稱（如 X-API-KEY）
) {
}