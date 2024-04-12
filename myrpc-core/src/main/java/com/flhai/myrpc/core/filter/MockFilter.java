package com.flhai.myrpc.core.filter;

import com.flhai.myrpc.core.api.Filter;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.util.MethodUtils;
import com.flhai.myrpc.core.util.MockUtil;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class MockFilter implements Filter {
    @Override
    public Object preFilter(RpcRequest request) {
        Method method = findMethod(request.getService(), request.getMethodSign());
        Class returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        return MockUtil.mock(returnType, genericReturnType);
    }

    @SneakyThrows
    private Method findMethod(String service, String methodSign) {
        Class interfaceClass = Class.forName(service);
        Method[] methods = interfaceClass.getMethods();
        for (Method method : methods) {
            if(MethodUtils.checkLocalMethod(method)) {
                continue;
            }

            if (methodSign.equals(MethodUtils.methodSign(method))) {
                return method;
            }
        }
        return null;
    }

    @Override
    public RpcResponse postFilter(RpcRequest request, RpcResponse response, Object result) {
        return null;
    }
}
