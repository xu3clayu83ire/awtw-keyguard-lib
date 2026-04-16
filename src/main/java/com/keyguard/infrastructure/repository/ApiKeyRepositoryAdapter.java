package com.keyguard.infrastructure.repository;

import com.keyguard.domain.model.ApiKey;
import com.keyguard.domain.model.KeyStatus;
import com.keyguard.domain.service.ApiKeyRecordStore;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ApiKeyRecordStore 的實作類（Adapter）。
 * 介接領域服務介面與 Spring Data JPA Repository。
 */
@Repository
public class ApiKeyRepositoryAdapter implements ApiKeyRecordStore {

    private final SpringDataApiKeyRepository springDataApiKeyRepository;
    private final Clock clock; // 注入 Clock 以支援測試時替換時間

    public ApiKeyRepositoryAdapter(SpringDataApiKeyRepository springDataApiKeyRepository, Clock clock) {
        this.springDataApiKeyRepository = springDataApiKeyRepository;
        this.clock = clock;
    }

    /**
     * 建立并儲存新的 API Key 記錄。
     */
    @Override
    public ApiKey issue(String vendorName, String keyHash, List<String> allowIps, String createdUser) {
        LocalDateTime now = LocalDateTime.now(clock);
        ApiKeyEntity entity = ApiKeyEntity.issue(vendorName, keyHash, allowIps, createdUser, now);
        return springDataApiKeyRepository.save(entity).toDomain();
    }

    /**
     * 依雜湊查找付活中的 API Key。
     */
    @Override
    public Optional<ApiKey> findActiveByHash(String keyHash) {
        return springDataApiKeyRepository.findByKeyHashAndStatus(keyHash, KeyStatus.ACTIVE)
                .map(ApiKeyEntity::toDomain);
    }

    /**
     * 撤銷指定 ID 的 API Key，回傳更新後的領域物件。
     */
    @Override
    public Optional<ApiKey> revoke(Long id, String updatedUser) {
        return springDataApiKeyRepository.findById(id)
                .map(entity -> {
                    entity.revoke(updatedUser, LocalDateTime.now(clock));
                    return springDataApiKeyRepository.save(entity).toDomain();
                });
    }
}