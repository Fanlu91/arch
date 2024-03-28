package com.flhai.myrpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.flhai.myrpc.core.api.RpcContext;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.util.MethodUtils;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.flhai.myrpc.core.util.TypeUtils.castMethodReturnType;

public class MyInvocationHandler implements InvocationHandler {

    Class<?> serviceClass;
    RpcContext rpcContext;
    List<String> providers;

    final MediaType JSONTYPE = MediaType.parse("application/json; charset=utf-8");

    public MyInvocationHandler(Class<?> serviceClass, RpcContext rpcContext, List<String> providers) {
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

        List<String> route = rpcContext.getRouter().route(providers);
        String url = (String) rpcContext.getLoadBalancer().choose(route);
        System.out.println("loadBalancer.choose url = " + url);
        RpcResponse rpcResponse = post(rpcRequest, url);
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return castMethodReturnType(method, data);
        } else {
            Exception ex = rpcResponse.getEx();
//            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 10, TimeUnit.MINUTES))
            .readTimeout(1000, TimeUnit.SECONDS)
            .writeTimeout(1000, TimeUnit.SECONDS)
            .connectTimeout(1000, TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println("reqJson = " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        try {
            String responseJson = okHttpClient.newCall(request).execute().body().string();
            System.out.println("responseJson = " + responseJson);
            return JSON.parseObject(responseJson, RpcResponse.class);
        } catch (ClassCastException e) {
            return new RpcResponse(false, null, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
