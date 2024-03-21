package com.flhai.myrpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.util.MethodUtils;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static com.flhai.myrpc.core.util.TypeUtils.cast;

public class MyInvocationHandler implements InvocationHandler {

    Class<?> serviceClass;
    final MediaType JSONTYPE = MediaType.parse("application/json; charset=utf-8");

    public MyInvocationHandler(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(serviceClass.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setParams(args);

        RpcResponse rpcResponse = post(rpcRequest);
        if(rpcResponse.isStatus()){
            Object data = rpcResponse.getData();
            System.out.println("data = " + data);
            System.out.println("method.getReturnType() = " + method.getReturnType());
            System.out.println("method.getGenericReturnType = " + method.getGenericReturnType());
            if(data instanceof JSONObject jsonResult) {
                System.out.println("jsonResult = " + jsonResult);
                return jsonResult.toJavaObject(method.getGenericReturnType());
            }else if(data instanceof JSONArray jsonArray){
                System.out.println("jsonArray = " + jsonArray);
                return jsonArray.toJavaObject(method.getGenericReturnType());
            }else {
                System.out.println("cast data = " + data);
                return cast(data,method.getReturnType());
            }
        }else {
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
    private RpcResponse post(RpcRequest rpcRequest) {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println("reqJson = " + reqJson);
        Request request = new Request.Builder()
                .url("http://localhost:8080")
                .post(RequestBody.create(reqJson,JSONTYPE))
                .build();
        try {
            String responseJson = okHttpClient.newCall(request).execute().body().string();
            System.out.println("responseJson = " + responseJson);
            return JSON.parseObject(responseJson,RpcResponse.class);
        } catch (ClassCastException e) {
            return new RpcResponse(false, null, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
