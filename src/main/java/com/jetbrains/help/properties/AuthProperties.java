package com.jetbrains.help.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Zhuang
 * @since 2024/7/9
 */
@Data
@Configuration
@ConfigurationProperties("auth")
public class AuthProperties {
    private boolean enable;
    private String type;
    private Map<String, String> config;

    public String get(String name) {
        return this.config.get(name);
    }
}
