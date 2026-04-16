package com.keyguard.infrastructure.repository;

import com.keyguard.domain.model.ApiKey;
import com.keyguard.domain.model.KeyStatus;
import com.keyguard.domain.service.ApiKeyRecordStore;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ApiKeyRepositoryAdapter implements ApiKeyRecordStore {

    private final SpringDataApiKeyRepository springDataApiKeyRepository;
    private final Clock clock;

    public ApiKeyRepositoryAdapter(SpringDataApiKeyRepository springDataApiKeyRepository, Clock clock) {
        this.springDataApiKeyRepository = springDataApiKeyRepository;
        this.clock = clock;
    }

    @Override
    public ApiKey issue(String vendorName, String keyHash, List<String> allowIps, String createdUser) {
        LocalDateTime now = LocalDateTime.now(clock);
        ApiKeyEntity entity = ApiKeyEntity.issue(vendorName, keyHash, allowIps, createdUser, now);
        return springDataApiKeyRepository.save(entity).toDomain();
    }

    @Override
    public Optional<ApiKey> findActiveByHash(String keyHash) {
        return springDataApiKeyRepository.findByKeyHashAndStatus(keyHash, KeyStatus.ACTIVE)
                .map(ApiKeyEntity::toDomain);
    }

    @Override
    public Optional<ApiKey> revoke(Long id, String updatedUser) {
        return springDataApiKeyRepository.findById(id)
                .map(entity -> {
                    entity.revoke(updatedUser, LocalDateTime.now(clock));
                    return springDataApiKeyRepository.save(entity).toDomain();
                });
    }
}