package com.flhai.myrpc.core.http;

import com.alibaba.fastjson.JSON;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpInvoker implements HttpInvoker {
    final static MediaType JSONTYPE = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient okHttpClient;

    public OkHttpInvoker(int timeout) {
        okHttpClient = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 10, TimeUnit.MINUTES))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        log.debug("post reqJson = " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        try {
            String responseJson = okHttpClient.newCall(request).execute().body().string();
            log.debug("post responseJson = " + responseJson);
            return JSON.parseObject(responseJson, RpcResponse.class);
        } catch (ClassCastException e) {
            log.error("ClassCastException while post: ", e);
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String post(String requestString, String url) {
        log.debug("post String = " + requestString);
//        讲 string 转换成 JSONString 画蛇添足了， 导致下游无法正常解析
//        String reqJson = JSON.toJSONString(requestString);
//        log.debug("post reqJson = " + reqJson);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestString, JSONTYPE))
                .build();

        try {
            String responseJson = okHttpClient.newCall(request).execute().body().string();
            return responseJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            String responseJson = okHttpClient.newCall(request).execute().body().string();
            log.debug("get responseJson = " + responseJson);
            return responseJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
