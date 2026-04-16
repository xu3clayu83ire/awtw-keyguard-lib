package com.keyguard.application.dto;

import java.time.LocalDateTime;

public record RevokeApiKeyResponse(
        Long id,
        String vendorName,
        String status,
        String updatedUser,
        LocalDateTime updatedAt
) {
}