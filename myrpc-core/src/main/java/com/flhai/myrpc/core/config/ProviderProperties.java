package com.flhai.myrpc.core.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "myrpc.provider")
@RefreshScope
@ImportAutoConfiguration(RefreshAutoConfiguration.class)
public class ProviderProperties {
    // for provider
    Map<String, String> metas;

    String test;
}