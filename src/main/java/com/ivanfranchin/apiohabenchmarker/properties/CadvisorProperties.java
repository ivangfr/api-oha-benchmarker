package com.ivanfranchin.apiohabenchmarker.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cadvisor")
public record CadvisorProperties(
        boolean enabled,
        boolean openBrowser,
        String browserCommand) {
}
