package com.flhai.myrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "myrpc.app")
public class AppProperties {

    // for app instance
    private String id;

    private String namespace;

    private String env;

}