package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MyrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyrpcDemoProviderApplication.class, args);
    }

    // use http + json to communicate
    @RequestMapping("/")
    public RpcResponse invoke(RpcRequest request) {
        // find the service


        return new RpcResponse();
    }
}
