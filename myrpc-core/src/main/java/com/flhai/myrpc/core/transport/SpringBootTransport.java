package com.flhai.myrpc.core.transport;

import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringBootTransport {
    @Autowired
    ProviderInvoker providerInvoker;

    // use http + json to communicate
    @RequestMapping("/myrpc")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        // find the service
        return providerInvoker.invokeRequest(request);
    }

   

}
