package com.keyguard.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keyguard")
public record KeyGuardProperties(
        String prefix,
        String protectedPathPrefix,
        String forwardedForHeader,
        String apiKeyHeader
) {
}