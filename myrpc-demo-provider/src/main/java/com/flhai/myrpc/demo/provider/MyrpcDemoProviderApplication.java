package com.flhai.myrpc.demo.provider;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.flhai.myrpc.core.config.ProviderConfig;
import com.flhai.myrpc.core.config.ProviderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@Import({ProviderConfig.class})
@RestController
@EnableApolloConfig
public class MyrpcDemoProviderApplication {

    @Value("${myrpc.provider.test}")
    String test;

    @Autowired
    ProviderProperties providerProperties;

    @RequestMapping("/test")
    public String test() {
        return "@value 注解值: "+ test + " \nproviderProperties值: " + providerProperties.getTest();
    }

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

