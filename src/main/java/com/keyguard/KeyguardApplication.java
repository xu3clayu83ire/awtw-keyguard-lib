package com.keyguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * KeyGuard 應用程式入口點。
 * 啟動 Spring Boot 並掃描所有 @ConfigurationProperties 設定類別。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class KeyguardApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyguardApplication.class, args);
    }
}