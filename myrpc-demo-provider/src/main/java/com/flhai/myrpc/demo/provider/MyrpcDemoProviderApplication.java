package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.core.config.ProviderConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ProviderConfig.class})
public class MyrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyrpcDemoProviderApplication.class, args);
    }

//    @Bean
//    ApplicationRunner runner() {
//        return args -> {
//            request.setService("com.flhai.myrpc.demo.api.OrderService");
//            request.setMethod("findOrderById");
//            request.setParams(new Object[]{404});

//            request.setService("com.flhai.myrpc.demo.api.UserService");
//            request.setMethod("findById");
//            request.setParams(new Object[]{100, "flhai"});
//            response = invoke(request);
//            log.info("return: " + response.getData());

//            var request = new RpcRequest();
//            request.setService("com.flhai.myrpc.demo.api.UserService");
//            request.setMethodSign("findById@2_int_java.lang.String");
//            request.setParams(new Object[]{100, "fhai"});
//            RpcResponse response = invoke(request);
//            System.out.println("return: " + response.toString());

//            var request = new RpcRequest();
//            request.setService("com.flhai.myrpc.demo.api.UserService");
//            request.setMethodSign("getList@1_java.util.List");
//            request.setParams(new Object[]{List.of(
//                    new User(100, "KK100"),
//                    new User(101, "KK101"),
//                    new User(102, "KK102"))});
//            RpcResponse response = invoke(request);
//            System.out.println("return: " + response.toString());
//        };
//    }
}

