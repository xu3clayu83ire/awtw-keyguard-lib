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

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyAuthenticationService apiKeyAuthenticationService;
    private final KeyGuardProperties keyGuardProperties;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(
            ApiKeyAuthenticationService apiKeyAuthenticationService,
            KeyGuardProperties keyGuardProperties,
            ObjectMapper objectMapper
    ) {
        this.apiKeyAuthenticationService = apiKeyAuthenticationService;
        this.keyGuardProperties = keyGuardProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(keyGuardProperties.protectedPathPrefix());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String rawKey = request.getHeader(keyGuardProperties.apiKeyHeader());
        String requestIp = extractRequestIp(request);

        ApiKey apiKey = apiKeyAuthenticationService.authenticate(rawKey, requestIp).orElse(null);
        if (apiKey == null) {
            writeUnauthorizedResponse(response);
            return;
        }

        request.setAttribute("keyguard.apiKeyId", apiKey.id());
        request.setAttribute("keyguard.vendorName", apiKey.vendorName());
        filterChain.doFilter(request, response);
    }

    private String extractRequestIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(keyGuardProperties.forwardedForHeader());
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of(
                "message", "API key invalid, revoked, or request IP is not allowed"
        ));
    }
}