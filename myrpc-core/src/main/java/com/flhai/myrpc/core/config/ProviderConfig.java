package com.flhai.myrpc.core.config;

import com.flhai.myrpc.core.api.RegistryCenter;
import com.flhai.myrpc.core.provider.ProviderBootstrap;
import com.flhai.myrpc.core.provider.ProviderInvoker;
import com.flhai.myrpc.core.registry.zk.ZkRegistryCenter;
import com.flhai.myrpc.core.transport.SpringBootTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
@Import({SpringBootTransport.class, ProviderProperties.class, AppProperties.class})
public class ProviderConfig {

    @Value("${server.port:8088}")
    private String port;

    @Bean
    ProviderBootstrap providerBootstrap(@Autowired AppProperties ap,
                                        @Autowired ProviderProperties pp) {
        return new ProviderBootstrap(port, ap, pp);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean()
    public RegistryCenter providerRegistryCenter() {
        return new ZkRegistryCenter();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerConfigRunner(@Autowired ProviderBootstrap providerBootstrap) {
        return args -> {
            providerBootstrap.start();
        };
    }
}
