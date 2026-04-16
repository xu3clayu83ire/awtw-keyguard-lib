package com.keyguard.infrastructure.config;

import com.keyguard.interfaces.filter.ApiKeyAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * Spring Security 設定類。
 * 配置無狀態式安全策略，並插入 API Key 驗證過濾器。
 */
@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable()) // 停用 CSRF（無狀態 API 不需要）
                                .httpBasic(httpBasic -> httpBasic.disable()) // 停用 HTTP Basic 登入
                                .formLogin(form -> form.disable()) // 停用表單登入
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 無狀態模式
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/h2-console/**", "/api/v1/keys/**").permitAll() // 管理路徑與
                                                                                                                  // H2
                                                                                                                  // 不需認證
                                                .anyRequest().permitAll())
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // 允許同源 iframe（H2
                                                                                                       // Console 需要）
                                .addFilterBefore(apiKeyAuthenticationFilter, AnonymousAuthenticationFilter.class) // 插入自訂
                                                                                                                  // API
                                                                                                                  // Key
                                                                                                                  // 過濾器
                                .build();
        }
}