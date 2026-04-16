package com.keyguard.interfaces.rest;

import com.keyguard.application.dto.ApiKeyResponse;
import com.keyguard.application.dto.CreateApiKeyRequest;
import com.keyguard.application.dto.RevokeApiKeyRequest;
import com.keyguard.application.dto.RevokeApiKeyResponse;
import com.keyguard.application.usecase.KeyIssuanceService;
import com.keyguard.application.usecase.RevokeApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/keys")
public class KeyManagementController {

    private final KeyIssuanceService keyIssuanceService;
    private final RevokeApiKeyService revokeApiKeyService;

    public KeyManagementController(
            KeyIssuanceService keyIssuanceService,
            RevokeApiKeyService revokeApiKeyService
    ) {
        this.keyIssuanceService = keyIssuanceService;
        this.revokeApiKeyService = revokeApiKeyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyResponse create(@Valid @RequestBody CreateApiKeyRequest request) {
        return keyIssuanceService.generate(request);
    }

    @PatchMapping("/{id}/revoke")
    public RevokeApiKeyResponse revoke(
            @PathVariable Long id,
            @Valid @RequestBody RevokeApiKeyRequest request
    ) {
        return revokeApiKeyService.revoke(id, request.updatedUser());
    }
}