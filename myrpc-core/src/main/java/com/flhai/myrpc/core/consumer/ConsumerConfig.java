package com.flhai.myrpc.core.consumer;

import com.flhai.myrpc.core.api.LoadBalancer;
import com.flhai.myrpc.core.api.RegistryCenter;
import com.flhai.myrpc.core.api.Router;
import com.flhai.myrpc.core.cluster.GreyRouter;
import com.flhai.myrpc.core.cluster.RoundRibonLoadBalancer;
import com.flhai.myrpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

    @Value("${myrpc.providers}")
    String services;

    @Value("${app.greyRate}")
    int greyRate;

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
        return new GreyRouter(greyRate);
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


}
