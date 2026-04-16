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

/**
 * API Key 的 JPA 實體，對應資料庫表 api_keys。
 * IP 白名單以逗號分隔字串儲存於單一欄位中。
 */
@Entity
@Table(name = "api_keys")
public class ApiKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_name", nullable = false, length = 100)
    private String vendorName; // 廠商名稱

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash; // SHA-256 雜湊後的金鑰（唯一鍵）

    @Column(name = "allow_ip", length = 500)
    private String allowIp; // IP 白名單，以逗號分隔存儲

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private KeyStatus status; // 金鑰狀態

    @Column(name = "created_user", nullable = false, length = 20)
    private String createdUser; // 建立者

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 建立時間

    @Column(name = "updated_user", length = 20)
    private String updatedUser; // 最後更新者

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 最後更新時間

    /**
     * 建立一筆新的 ACTIVE 狀態 API Key 實體。
     */
    public static ApiKeyEntity issue(String vendorName, String keyHash, List<String> allowIps, String createdUser,
            LocalDateTime createdAt) {
        ApiKeyEntity entity = new ApiKeyEntity();
        entity.vendorName = vendorName;
        entity.keyHash = keyHash;
        entity.allowIp = toAllowIp(allowIps);
        entity.status = KeyStatus.ACTIVE;
        entity.createdUser = createdUser;
        entity.createdAt = createdAt;
        return entity;
    }

    /**
     * 將金鑰狀態改為 REVOKED。
     */
    public void revoke(String updatedUser, LocalDateTime updatedAt) {
        this.status = KeyStatus.REVOKED;
        this.updatedUser = updatedUser;
        this.updatedAt = updatedAt;
    }

    /**
     * 將 JPA 實體轉換為領域物件。
     */
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
                updatedAt);
    }

    /**
     * 將 IP 列表轉換為逗號分隔字串。
     */
    private static String toAllowIp(List<String> allowIps) {
        if (allowIps == null || allowIps.isEmpty()) {
            return null;
        }

        return allowIps.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(","));
    }

    /**
     * 將逗號分隔字串解析為 IP 列表。
     */
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