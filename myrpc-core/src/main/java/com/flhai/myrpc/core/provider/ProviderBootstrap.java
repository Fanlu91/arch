package com.flhai.myrpc.core.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.util.MethodUtils;
import com.flhai.myrpc.core.util.TypeUtils;
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
import java.util.Arrays;
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
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(MyProvider.class);
        providers.values().forEach(provider -> {
            getInterfaces(provider);
        });

    }

    private void getInterfaces(Object provider) {
        Class<?>[] interfaces = provider.getClass().getInterfaces();
        Arrays.stream(interfaces).forEach(providerInterface -> {
            Method[] methods = providerInterface.getMethods();
            for (Method method : methods) {
                if (MethodUtils.checkLocalMethod(method)) {
                    continue;
                }
                createProvider(providerInterface, provider, method);
            }
        });
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
            //  Method method = bean.getClass().getMethod(request.getMethod());
            // Deprecated method
            //  Method method = findMethod(bean, request.getMethodSign(), request.getParams());
            ProviderMeta providerMeta = getProviderMeta(request, methodSign, providerMetas);
            Method method = providerMeta.getMethod();
            Object[] params = processParams(request.getParams(), method.getParameterTypes());
//            System.out.println("params length = " + params.length);
//            System.out.println(params[0].getClass().getName());
//            System.out.println(providerMeta.getSignName());
            Object result = method.invoke(providerMeta.getServiceImpl(), params);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        } catch (InvocationTargetException e) {
            // 这里的e是InvocationTargetException 反射异常
            // 我们从中取出原始异常
            rpcResponse.setStatus(false);
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
            rpcResponse.setData(e.getTargetException().getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            rpcResponse.setStatus(false);
            rpcResponse.setEx(e);
        }
        return rpcResponse;
    }

    private static ProviderMeta getProviderMeta(RpcRequest request, String methodSign, List<ProviderMeta> providerMetas) {
        ProviderMeta providerMeta = providerMetas.stream().filter(
                        p -> p.getSignName().equals(methodSign)
                ).findFirst()
                .orElseThrow(() -> new RuntimeException("no such method with sign"
                        + methodSign + " in provider " + request.getService()));
        return providerMeta;
    }

    private Object[] processParams(Object[] params, Class<?>[] parameterTypes) {
        if (params == null || params.length == 0) {
            return new Object[0];
        }
        Object[] result = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            result[i] = TypeUtils.cast(params[i], parameterTypes[i]);
//            System.out.println("-----param [i] = " + result[i]);
        }

        return result;
    }

    /**
     * 通过方法名和参数类型查找方法, 用于处理重载的情况.
     * 已替换为method sign的方式
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