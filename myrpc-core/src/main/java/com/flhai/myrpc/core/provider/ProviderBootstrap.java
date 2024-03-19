package com.flhai.myrpc.core.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import meta.ProviderMeta;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;

    // 据说可以省略
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();


    @PostConstruct // 相当于 init method
    public void buildProvider() {
//        System.out.println("-----buildProvider");
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(MyProvider.class);
        providers.values().forEach(provider -> {
            getOnlyInterface(provider);
        });

    }

    // 只继承了一个接口
    private void getOnlyInterface(Object o) {
        Class<?>[] interfaces = o.getClass().getInterfaces();
        if (interfaces.length != 1) {
            throw new RuntimeException("provider must implement only one interface");
        }
        Class<?> providerInterface = interfaces[0];
        Method[] methods = providerInterface.getMethods();
        for (Method method : methods) {
            if (MethodUtils.checkLocalMethod(method)) {
                continue;
            }
            createProvider(providerInterface, o, method);
        }
    }

    private void createProvider(Class<?> providerInterface, Object o, Method method) {
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setMethod(method);
        providerMeta.setSignName(MethodUtils.methodSign(method));
        providerMeta.setServiceImpl(o);
        System.out.println("Created provider, providerMeta = " + providerMeta);
        skeleton.add(providerInterface.getCanonicalName(), providerMeta);
    }

    public RpcResponse invokeRequest(RpcRequest request) {
        String methodSign = request.getMethodSign();

        RpcResponse rpcResponse = new RpcResponse();
        // 通过接口名找到对应的实现类, 通过方法签名找到重载中对应的方法
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {
            // getMethod 需要参数类型（方法可能重载），这里还没有拿到
//            Method method = bean.getClass().getMethod(request.getMethod());
//            Method method = findMethod(bean, request.getMethodSign(), request.getParams());
            ProviderMeta providerMeta = providerMetas.stream().filter(
                            p -> p.getSignName().equals(methodSign)
                    ).findFirst()
                    .orElseThrow(() -> new RuntimeException("no such method with sign" + methodSign + " in provider " + request.getService()));
            Method method = providerMeta.getMethod();
            Object result = method.invoke(providerMeta.getServiceImpl(), request.getParams());
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        } catch (InvocationTargetException e) {
            // 这里的e是InvocationTargetException 反射异常
            // 我们从中取出原始异常
            rpcResponse.setStatus(false);
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
            rpcResponse.setData(e.getTargetException().getMessage());
        } catch (Exception e) {
            // 这里的e是InvocationTargetException
            // 我们从中取出原始异常
            rpcResponse.setStatus(false);
            rpcResponse.setEx(e);
        }
        return rpcResponse;
    }

    /**
     * 通过方法名和参数类型查找方法, 用于处理重载的情况.
     *
     * @param bean
     * @param methodName
     * @param params
     * @return
     * @deprecated
     */
    @Deprecated
    private Method findMethod(Object bean, String methodName, Object... params) {
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (params.length == 0)
                    return method;
                else { // 处理重载的情况
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != params.length)
                        continue;
                    boolean isSameMethod = true;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        // check if paramterType match with param type
                        if (!MethodUtils.isCompatibleType(parameterTypes[i], params[i].getClass())) {
                            isSameMethod = false;
                            break;
                        }
                    }
                    if (isSameMethod)
                        return method;
                }
            }
        }
        return null;
    }

}