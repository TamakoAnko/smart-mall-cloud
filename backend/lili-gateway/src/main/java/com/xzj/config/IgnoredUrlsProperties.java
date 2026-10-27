package com.xzj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "lili.gateway")
public class IgnoredUrlsProperties {

    private String tokenHeader = "AccessToken";

    /** Development mode bypasses gateway authentication during integration. */
    private boolean developmentMode = true;

    private List<String> ignoredUrls = new ArrayList<>();
}