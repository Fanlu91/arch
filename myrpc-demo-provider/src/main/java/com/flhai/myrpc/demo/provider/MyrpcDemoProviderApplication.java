package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class MyrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyrpcDemoProviderApplication.class, args);
    }

    // use http + json to communicate
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        // find the service
        return invokeRequest(request);

//        return new RpcResponse();
    }

    private RpcResponse invokeRequest(RpcRequest request) {
        Object bean = skeleton.get(request.getService());
        try {
            Method method = bean.getClass().getMethod(request.getMethod());
            Object result = method.invoke(bean, request.getParams());

            return new RpcResponse(true, result);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct
    public void buildProvider(){
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(MyProvider.class);
        providers.forEach((k,v)->{
            System.out.println(k);
        });

        providers.values().forEach(provider->{
            // get all interfaces of the provider
            Class<?>[] interfaces = provider.getClass().getInterfaces();
            // skeleton 有了接口的全限定名和接口的实现类
            for (Class<?> i : interfaces) {
                skeleton.put(i.getCanonicalName(), i);
            }
        });

    }
}
