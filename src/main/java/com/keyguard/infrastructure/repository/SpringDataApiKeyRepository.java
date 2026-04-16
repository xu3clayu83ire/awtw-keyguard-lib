package com.keyguard.infrastructure.repository;

import com.keyguard.domain.model.KeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA Repository，提供 API Key 的資料庫操作。
 */
public interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {

    /**
     * 依雜湊字串與狀態查詢 API Key。
     */
    Optional<ApiKeyEntity> findByKeyHashAndStatus(String keyHash, KeyStatus status);
}