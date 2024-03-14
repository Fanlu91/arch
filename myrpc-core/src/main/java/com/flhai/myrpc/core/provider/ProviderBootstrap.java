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

    @PostConstruct
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
        try {
//            Method method = bean.getClass().getMethod(request.getMethod());
            Method method = findMethod(bean, request.getMethod());
            Object result = method.invoke(bean, request.getParams());


            return new RpcResponse(true, result);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Object bean, String methodName) {
//        System.out.println("bean: " + bean.getClass().getName());
//        System.out.println("methodName: " + methodName);
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
//            System.out.println("method: " + method.getName());
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }


}
