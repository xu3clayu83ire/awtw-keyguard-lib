package com.keyguard.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 撤銷 API Key 的請求 DTO。
 */
public record RevokeApiKeyRequest(
                @NotBlank @Size(max = 20) String updatedUser // 撤銷操作者（必填）
) {
}