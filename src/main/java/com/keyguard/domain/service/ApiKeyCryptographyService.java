package com.keyguard.domain.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * API Key 密碼學服務。
 * 負責生成隨機原始金鑰，以及對金鑰進行 SHA-256 雜湊。
 */
@Service
public class ApiKeyCryptographyService {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int KEY_BYTE_LENGTH = 32; // 隨機位元長度，產生 64 位十六進制字串
    private static final SecureRandom SECURE_RANDOM = new SecureRandom(); // 安全隨機數產生器

    /**
     * 依據字首（prefix）生成原始 API Key。
     * 格式：{prefix}_{64位十六進制隨機字串}
     */
    public String generateRawKey(String prefix) {
        byte[] randomBytes = new byte[KEY_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return prefix + "_" + HexFormat.of().formatHex(randomBytes);
    }

    /**
     * 對原始金鑰進行 SHA-256 雜湊，回傳十六進制字串。
     * 資料庫只儲存雜湊後的金鑰，避免原始金鑰洩漏。
     */
    public String hash(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}