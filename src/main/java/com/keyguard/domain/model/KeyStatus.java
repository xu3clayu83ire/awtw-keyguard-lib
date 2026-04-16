package com.keyguard.domain.model;

/**
 * API Key 的狀態列舉。
 */
public enum KeyStatus {
    /** 啟用中，可正常使用 */
    ACTIVE,
    /** 已撤銷，無法再使用 */
    REVOKED
}