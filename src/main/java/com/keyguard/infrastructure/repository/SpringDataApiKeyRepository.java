package com.keyguard.infrastructure.repository;

import com.keyguard.domain.model.KeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {

    Optional<ApiKeyEntity> findByKeyHashAndStatus(String keyHash, KeyStatus status);
}