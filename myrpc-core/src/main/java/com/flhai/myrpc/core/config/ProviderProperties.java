package com.flhai.myrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "myrpc.provider")
public class ProviderProperties {
    // for provider
    Map<String, String> metas;
}