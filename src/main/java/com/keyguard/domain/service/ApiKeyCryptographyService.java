package com.keyguard.domain.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Service
public class ApiKeyCryptographyService {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int KEY_BYTE_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generateRawKey(String prefix) {
        byte[] randomBytes = new byte[KEY_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return prefix + "_" + HexFormat.of().formatHex(randomBytes);
    }

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