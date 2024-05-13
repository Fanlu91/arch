package com.flhai.myrpc.core.config;

import com.flhai.myrpc.core.api.*;
import com.flhai.myrpc.core.cluster.GreyRouter;
import com.flhai.myrpc.core.cluster.RoundRibonLoadBalancer;
import com.flhai.myrpc.core.consumer.ConsumerBootstrap;
import com.flhai.myrpc.core.filter.ParameterFilter;
import com.flhai.myrpc.core.registry.RegistryCenter;
import com.flhai.myrpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
@Import({ConsumerProperties.class, AppProperties.class})
public class ConsumerConfig {

    @Autowired
    ConsumerProperties consumerProperties;

    @Autowired
    AppProperties appProperties;

//    @Value("${myrpc.providers}")
//    String services;

    @Bean
    public ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerConfigRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return args -> {
            consumerBootstrap.startApplication();
        };
    }

    @Bean
    public LoadBalancer loadBalancer() {
//        return new RandomLoadBalancer();
        return new RoundRibonLoadBalancer();
    }


    @Bean
    public Router router() {
        return new GreyRouter(consumerProperties.getGreyRatio());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumerRegistryCenter() {
//        return new RegistryCenter.StaticRegistryCenter(List.of(services.split(",")));
        return new ZkRegistryCenter();
    }

//    @Bean
//    public Filter filter1() {
//        return new CacheFilter();
//    }

//    @Bean
//    public Filter filter2() {
//        return new MockFilter();
//    }

    @Bean
    public Filter paramFilter() {
        return new ParameterFilter();
    }

    @Bean
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.id", appProperties.getId());
        context.getParameters().put("app.namespace", appProperties.getNamespace());
        context.getParameters().put("app.env", appProperties.getEnv());
        context.setConsumerProperties(consumerProperties);
        return context;
    }
}
