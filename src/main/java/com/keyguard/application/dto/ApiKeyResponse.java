package com.keyguard.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiKeyResponse(
        Long id,
        String vendorName,
        String rawKey,
        String keyHash,
        List<String> allowIps,
        String status,
        String createdUser,
        LocalDateTime createdAt
) {
}