package com.flhai.myrpc.core.consumer;

import com.flhai.myrpc.core.api.*;
import com.flhai.myrpc.core.consumer.http.OkHttpInvoker;
import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.util.MethodUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import static com.flhai.myrpc.core.util.TypeUtils.castMethodReturnType;

@Slf4j
public class MyInvocationHandler implements InvocationHandler {

    Class<?> serviceClass;
    RpcContext rpcContext;
    List<InstanceMeta> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

    public MyInvocationHandler(Class<?> serviceClass, RpcContext rpcContext, List<InstanceMeta> providers) {
        this.serviceClass = serviceClass;
        this.rpcContext = rpcContext;
        this.providers = providers;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(serviceClass.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setParams(args);
        List<InstanceMeta> route = rpcContext.getRouter().route(providers);
        List<Filter> filters = rpcContext.getFilters();
        for (Filter filter : filters) {
            Object preResult = filter.preFilter(rpcRequest);
            if (preResult != null) {
                log.debug(filter.getClass().getName() + " ==> prefilter: " + preResult);
                return preResult;
            }
        }

        InstanceMeta instance = rpcContext.getLoadBalancer().choose(route);
        String url = instance.toUrl();
        log.debug("===> loadBalancer.choose url = " + url);
        RpcResponse rpcResponse = httpInvoker.post(rpcRequest, url);

        // 这里cache filter应该放在最后一个执行，否则缓存的结果可能不是最终结果，造成问题
        Object result = castReturnResult(method, rpcResponse);

        for (Filter filter : filters) {
            Object filterResult = filter.postFilter(rpcRequest, rpcResponse, result);
            if (filterResult != null) {
                return filterResult;
            }
        }

        return result;
    }

    private static Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return castMethodReturnType(method, data);
        } else {
            Exception exception = rpcResponse.getEx();
            if (exception instanceof RpcException ex) {
                throw ex;
            } else {
                throw new RpcException(exception, RpcException.UnknownEx);
            }

        }
    }

}
