package com.flhai.myrpc.demo.consumer;

import com.flhai.myrpc.demo.provider.MyrpcDemoProviderApplication;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest(classes = MyrpcDemoConsumerApplication.class)
@Disabled
class MyrpcDemoConsumerApplicationTests {
    static ConfigurableApplicationContext context;
    @BeforeAll
    static void init() {
        context = SpringApplication.run(MyrpcDemoProviderApplication.class,"--server.port=8084","--logging.level.myrpc=debug");
    }
    @Test
    void contextLoads() {
    }

    @PreDestroy
    void destory() {
        SpringApplication.exit(context, () -> 0);
    }
}
