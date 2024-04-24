package com.flhai.myrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "myrpc.consumer")
public class ConsumerProperties {

    // for ha and governance
    private int retries;

    private int timeout;

    private int halfOpenInitialDelay;

    private int halfOpenDelay;

    private int greyRatio;

}