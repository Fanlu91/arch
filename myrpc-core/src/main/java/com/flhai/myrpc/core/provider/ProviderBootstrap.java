package com.flhai.myrpc.core.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;

    // 据说可以省略
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Map<String, Object> skeleton = new HashMap<>();


    @PostConstruct // 相当于 init method
    public void buildProvider() {
        System.out.println("-----buildProvider");
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(MyProvider.class);
//        providers.forEach((k, v) -> {
//            System.out.println(k);
//        });

        providers.values().forEach(provider -> {
            getOnlyInterface(provider);
        });

    }

    // 只继承了一个接口
    private void getOnlyInterface(Object o) {
        Class<?>[] interfaces = o.getClass().getInterfaces();
        skeleton.put(interfaces[0].getCanonicalName(), o);
    }

    public RpcResponse invokeRequest(RpcRequest request) {
        Object bean = skeleton.get(request.getService());
        RpcResponse rpcResponse = new RpcResponse();;
        try {
            // getMethod 需要参数类型（方法可能重载），这里还没有拿到
//            Method method = bean.getClass().getMethod(request.getMethod());
            Method method = findMethod(bean, request.getMethod());
            Object result = method.invoke(bean, request.getParams());
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        }catch (InvocationTargetException e){
            // 这里的e是InvocationTargetException 反射异常
            // 我们从中取出原始异常
            rpcResponse.setStatus(false);
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
            rpcResponse.setData(e.getTargetException().getMessage());
        }catch (Exception e){
            // 这里的e是InvocationTargetException
            // 我们从中取出原始异常
            rpcResponse.setStatus(false);
            rpcResponse.setEx(e);
        }
        return rpcResponse;
    }

    private Method findMethod(Object bean, String methodName) {
//        System.out.println("bean: " + bean.getClass().getName());
//        System.out.println("methodName: " + methodName);
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}
