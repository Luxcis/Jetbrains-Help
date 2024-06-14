package com.jetbrains.help.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("help")
public class JetbrainsHelpProperties {
    private Boolean enableSecurity;
    private String productId;
    private String productVersion;
    private String defaultEmail;
    private String defaultLicenseName;
    private String defaultAssigneeName;
    private String defaultExpiryDate;
}
