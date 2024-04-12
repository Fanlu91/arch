package com.flhai.myrpc.core.provider;

import com.flhai.myrpc.core.api.RpcException;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.meta.ProviderMeta;
import com.flhai.myrpc.core.util.MethodUtils;
import com.flhai.myrpc.core.util.TypeUtils;
import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 把构建和执行provider分开
 */
@Data
public class ProviderInvoker {
    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootstrap bootstrap) {
        this.skeleton = bootstrap.getSkeleton();
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
//            log.debug("params length = " + params.length);
//            log.debug(params[0].getClass().getName());
//            log.debug(providerMeta.getSignName());
            Object result = method.invoke(providerMeta.getServiceImpl(), params);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        } catch (InvocationTargetException e) {
            // 这里的e是InvocationTargetException 反射异常
            // 我们从中取出原始异常
            rpcResponse.setStatus(false);
            rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
            rpcResponse.setData(e.getTargetException().getMessage());
        }catch (IllegalAccessException e) {
            rpcResponse.setStatus(false);
            rpcResponse.setEx(new RpcException(e.getMessage()));
            rpcResponse.setData(e.getMessage());
        }
        catch (Exception e) {
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
                .orElseThrow(() -> new RpcException("no such method with sign"
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
//            log.debug("-----param [i] = " + result[i]);
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
