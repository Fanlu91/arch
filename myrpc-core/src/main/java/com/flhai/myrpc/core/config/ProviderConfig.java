package com.flhai.myrpc.core.config;

import com.flhai.myrpc.core.registry.RegistryCenter;
import com.flhai.myrpc.core.provider.ProviderBootstrap;
import com.flhai.myrpc.core.provider.ProviderInvoker;
import com.flhai.myrpc.core.provider.TokenBucketLimiter;
import com.flhai.myrpc.core.registry.zk.ZkRegistryCenter;
import com.flhai.myrpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Slf4j
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

    /**
     * 令牌桶限流
     *
     * @param pp
     * @return
     */
    @Bean
    public TokenBucketLimiter tokenBucketLimiter(@Autowired ProviderProperties pp) {
        int refillInterval = Integer.parseInt(pp.getMetas().getOrDefault("refillInterval", "10000"));
        int maxTokens = Integer.parseInt(pp.getMetas().getOrDefault("maxToken", "10"));
        log.info("refillInterval:{}, maxTokens:{}", refillInterval, maxTokens);
        return new TokenBucketLimiter(refillInterval, maxTokens);
    }

    @Bean
//    @ConditionalOnMissingBean
    public ApolloChangedListener provider_apolloChangedListener() {
        return new ApolloChangedListener();
    }
}
