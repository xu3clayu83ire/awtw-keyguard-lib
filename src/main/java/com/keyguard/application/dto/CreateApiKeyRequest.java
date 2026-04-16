package com.keyguard.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateApiKeyRequest(
        @NotBlank @Size(max = 100) String vendorName,
        List<@NotBlank @Size(max = 50) String> allowIps,
        @NotBlank @Size(max = 20) String createdUser,
        @Size(max = 20) String prefix
) {
}