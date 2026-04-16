package com.keyguard.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record ApiKey(
        Long id,
        String vendorName,
        String keyHash,
        List<String> allowIps,
        KeyStatus status,
        String createdUser,
        LocalDateTime createdAt,
        String updatedUser,
        LocalDateTime updatedAt
) {

    public boolean isAccessibleFrom(String requestIp) {
        if (status != KeyStatus.ACTIVE) {
            return false;
        }

        if (allowIps == null || allowIps.isEmpty()) {
            return true;
        }

        String normalizedRequestIp = normalizeIp(requestIp);
        return allowIps.stream()
                .map(ApiKey::normalizeIp)
                .anyMatch(normalizedRequestIp::equals);
    }

    private static String normalizeIp(String ip) {
        return ip == null ? "" : ip.trim();
    }
}