package com.flhai.myrpc.core.consumer;

import com.flhai.myrpc.core.api.LoadBalancer;
import com.flhai.myrpc.core.api.Router;
import com.flhai.myrpc.core.cluster.RoundRibonLoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {
    @Bean
    public ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner configRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
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
        return Router.Default;
    }
}
