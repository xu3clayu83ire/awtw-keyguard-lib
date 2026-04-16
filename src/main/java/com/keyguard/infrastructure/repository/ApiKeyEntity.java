package com.keyguard.infrastructure.repository;

import com.keyguard.domain.model.ApiKey;
import com.keyguard.domain.model.KeyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "api_keys")
public class ApiKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_name", nullable = false, length = 100)
    private String vendorName;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @Column(name = "allow_ip", length = 500)
    private String allowIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private KeyStatus status;

    @Column(name = "created_user", nullable = false, length = 20)
    private String createdUser;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_user", length = 20)
    private String updatedUser;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static ApiKeyEntity issue(String vendorName, String keyHash, List<String> allowIps, String createdUser, LocalDateTime createdAt) {
        ApiKeyEntity entity = new ApiKeyEntity();
        entity.vendorName = vendorName;
        entity.keyHash = keyHash;
        entity.allowIp = toAllowIp(allowIps);
        entity.status = KeyStatus.ACTIVE;
        entity.createdUser = createdUser;
        entity.createdAt = createdAt;
        return entity;
    }

    public void revoke(String updatedUser, LocalDateTime updatedAt) {
        this.status = KeyStatus.REVOKED;
        this.updatedUser = updatedUser;
        this.updatedAt = updatedAt;
    }

    public ApiKey toDomain() {
        return new ApiKey(
                id,
                vendorName,
                keyHash,
                parseAllowIps(allowIp),
                status,
                createdUser,
                createdAt,
                updatedUser,
                updatedAt
        );
    }

    private static String toAllowIp(List<String> allowIps) {
        if (allowIps == null || allowIps.isEmpty()) {
            return null;
        }

        return allowIps.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(","));
    }

    private static List<String> parseAllowIps(String allowIp) {
        if (allowIp == null || allowIp.isBlank()) {
            return List.of();
        }

        return Arrays.stream(allowIp.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}