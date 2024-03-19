package com.flhai.myrpc.demo.consumer;

import com.flhai.myrpc.core.annotation.MyConsumer;
import com.flhai.myrpc.core.consumer.ConsumerConfig;
import com.flhai.myrpc.demo.api.OrderService;
import com.flhai.myrpc.demo.api.User;
import com.flhai.myrpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({ConsumerConfig.class})
@SpringBootApplication
public class MyrpcDemoConsumerApplication {

    @MyConsumer
    UserService userService;

    @MyConsumer
    OrderService orderService;

    public static void main(String[] args) {
        SpringApplication.run(MyrpcDemoConsumerApplication.class, args);
    }

    @Bean
    public ApplicationRunner runner() {
        return args -> {
            User user = userService.findById(1);
            System.out.println(user);
            User user2 = userService.findById(1, "flhai");
            System.out.println(user2);
            System.out.println(userService.getId());
            System.out.println(userService.getName());
            System.out.println(userService.getName(100));

//            Order order = orderService.findOrderById(1);
//            System.out.println(order);
//            Order exOrder = orderService.findOrderById(404);
//            System.out.println(exOrder);
        };
    }
}
