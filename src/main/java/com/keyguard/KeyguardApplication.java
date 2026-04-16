package com.keyguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KeyguardApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyguardApplication.class, args);
    }
}