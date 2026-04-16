package com.keyguard.interfaces.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyguard.application.usecase.ApiKeyAuthenticationService;
import com.keyguard.domain.model.ApiKey;
import com.keyguard.infrastructure.config.KeyGuardProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * API Key 驗證過濾器。
 * 對符合保護路徑前綴的請求進行 API Key 驗證，
 * 驗證通過後將廠商資訊帶入 Request Attribute。
 * 繼承 OncePerRequestFilter 保證每次請求僅執行一次。
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyAuthenticationService apiKeyAuthenticationService;
    private final KeyGuardProperties keyGuardProperties;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(
            ApiKeyAuthenticationService apiKeyAuthenticationService,
            KeyGuardProperties keyGuardProperties,
            ObjectMapper objectMapper) {
        this.apiKeyAuthenticationService = apiKeyAuthenticationService;
        this.keyGuardProperties = keyGuardProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 判斷請求路徑是否不需要過濾（非保護路徑則跳過驗證）。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(keyGuardProperties.protectedPathPrefix());
    }

    /**
     * 執行 API Key 驗證流程。
     * 1. 從 Header 取得原始金鑰
     * 2. 跟擷客戶端 IP
     * 3. 驗證成功則帶入 API Key 資訊至請求屬性並放行
     * 4. 驗證失敗則回傳 401
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String rawKey = request.getHeader(keyGuardProperties.apiKeyHeader());
        String requestIp = extractRequestIp(request);

        ApiKey apiKey = apiKeyAuthenticationService.authenticate(rawKey, requestIp).orElse(null);
        if (apiKey == null) {
            // 驗證失敗，回傳 401
            writeUnauthorizedResponse(response);
            return;
        }

        // 驗證成功，將廠商資訊帶入請求屬性
        request.setAttribute("keyguard.apiKeyId", apiKey.id());
        request.setAttribute("keyguard.vendorName", apiKey.vendorName());
        filterChain.doFilter(request, response);
    }

    /**
     * 提取請求的真實 IP。
     * 弘先使用 X-Forwarded-For Header（經過 proxy 的情況），否則取遠端 IP。
     */
    private String extractRequestIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(keyGuardProperties.forwardedForHeader());
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // 取進入路徑第一個 IP（真實客戶端）
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 回傳 401 未授權的 JSON 回應。
     */
    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of(
                "message", "API key invalid, revoked, or request IP is not allowed"));
    }
}