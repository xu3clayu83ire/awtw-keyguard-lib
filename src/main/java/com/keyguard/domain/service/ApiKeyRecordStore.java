package com.keyguard.domain.service;

import com.keyguard.domain.model.ApiKey;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRecordStore {

    ApiKey issue(String vendorName, String keyHash, List<String> allowIps, String createdUser);

    Optional<ApiKey> findActiveByHash(String keyHash);

    Optional<ApiKey> revoke(Long id, String updatedUser);
}