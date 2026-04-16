package com.keyguard.interfaces.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 受保護路徑的示範控制器。
 * 模擬一個需要通過 API Key 驗證才能存取的資源。
 * 驗證成功後，可從 Request Attribute 取得廠商資訊。
 */
@RestController
@RequestMapping("/api/v1/keys")
public class ApikeyDataController {

    /**
     * 取得指定 loginId 的資料，需搜止有效的 API Key。
     * 廠商資訊由過濾器婉入至 Request Attribute。
     */
    @GetMapping("/{loginId}")
    public Map<String, Object> getApikeyData(
            @PathVariable String loginId,
            @RequestAttribute("keyguard.vendorName") String vendorName, // 由過濾器婉入
            @RequestAttribute("keyguard.apiKeyId") Long apiKeyId) { // 由過濾器婉入
        return Map.of(
                "loginId", loginId,
                "vendorName", vendorName,
                "apiKeyId", apiKeyId,
                "message", "API key validated");
    }
}