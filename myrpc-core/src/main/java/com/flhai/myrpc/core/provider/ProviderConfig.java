package com.flhai.myrpc.core.provider;

import com.flhai.myrpc.core.api.RegistryCenter;
import com.flhai.myrpc.core.registry.ZkRegistryCenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfig {
    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter providerRegistryCenter() {
        return new ZkRegistryCenter();
    }
}
