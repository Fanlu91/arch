package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.provider.ProviderBootstrap;
import com.flhai.myrpc.core.provider.ProviderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class MyrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyrpcDemoProviderApplication.class, args);
    }


    @Autowired
    ProviderBootstrap providerBootstrap;

    // use http + json to communicate
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        // find the service
        return providerBootstrap.invokeRequest(request);
    }


    @Bean
    ApplicationRunner runner() {
        return args -> {
            var request = new RpcRequest();
//            request.setService("com.flhai.myrpc.demo.api.UserService");
//            request.setMethod("findById");
//            request.setParams(new Object[]{100});
//            RpcResponse response = invoke(request);
//            System.out.println("return: " + response.getData());

//            request.setService("com.flhai.myrpc.demo.api.OrderService");
//            request.setMethod("findOrderById");
//            request.setParams(new Object[]{404});

//            request.setService("com.flhai.myrpc.demo.api.UserService");
//            request.setMethod("findById");
//            request.setParams(new Object[]{100, "flhai"});
//            response = invoke(request);
//            System.out.println("return: " + response.getData());

//            request.setService("com.flhai.myrpc.demo.api.UserService");
//            request.setMethodSign("findById@2_int_java.lang.String");
//            request.setParams(new Object[]{100,"fhai"});
//            RpcResponse response = invoke(request);
//            System.out.println("return: " + response.toString());
        };
    }
}
