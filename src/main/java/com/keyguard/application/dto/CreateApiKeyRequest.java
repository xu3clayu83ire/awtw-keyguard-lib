package com.keyguard.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 核發 API Key 的請求 DTO。
 */
public record CreateApiKeyRequest(
                @NotBlank @Size(max = 100) String vendorName, // 廠商名稱（必填，最多 100 字）
                List<@NotBlank @Size(max = 50) String> allowIps, // IP 白名單（可空，空則放行所有 IP）
                @NotBlank @Size(max = 20) String createdUser, // 建立者（必填）
                @Size(max = 20) String prefix // 金鑰字首（可空，空則使用系統預設）
) {
}