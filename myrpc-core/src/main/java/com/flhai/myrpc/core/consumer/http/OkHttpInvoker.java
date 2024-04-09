package com.flhai.myrpc.core.consumer.http;

import com.alibaba.fastjson.JSON;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.consumer.HttpInvoker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpInvoker implements HttpInvoker {
    final MediaType JSONTYPE = MediaType.parse("application/json; charset=utf-8");

    private  OkHttpClient okHttpClient;

    public OkHttpInvoker() {
        okHttpClient = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 10, TimeUnit.MINUTES))
                .readTimeout(1000, TimeUnit.SECONDS)
                .writeTimeout(1000, TimeUnit.SECONDS)
                .connectTimeout(1000, TimeUnit.SECONDS)
                .build();
    }
    @Override
    public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        log.debug("reqJson = " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        try {
            String responseJson = okHttpClient.newCall(request).execute().body().string();
            log.debug("responseJson = " + responseJson);
            return JSON.parseObject(responseJson, RpcResponse.class);
        } catch (ClassCastException e) {
            return new RpcResponse(false, null, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
