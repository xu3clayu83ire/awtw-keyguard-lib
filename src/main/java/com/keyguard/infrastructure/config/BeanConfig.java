package com.keyguard.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 通用 Bean 設定類。
 * 提供可被注入的系統元件。
 */
@Configuration
public class BeanConfig {

    /**
     * 提供系統預設時區的 Clock Bean。
     * 使用注入而非直接呼叫，方便單元測試時替換為固定時間。
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}