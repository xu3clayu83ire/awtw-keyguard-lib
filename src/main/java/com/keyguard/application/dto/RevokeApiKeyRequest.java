package com.keyguard.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevokeApiKeyRequest(
        @NotBlank @Size(max = 20) String updatedUser
) {
}