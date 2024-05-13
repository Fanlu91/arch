package com.flhai.myrpc.core.consumer;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.consumer.http.OkHttpInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface HttpInvoker {
    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    public RpcResponse<?> post(RpcRequest rpcRequest, String url);

    public String post(String requestString, String url);

    public String get(String url);

    HttpInvoker Default = new OkHttpInvoker(500);

    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" =====>>>> httpGet: " + url);
        String respJson = Default.get(url);
        log.debug(" =====>>>> response: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        log.debug(" =====>>>> httpGet: " + url);
        String respJson = Default.get(url);
        log.debug(" =====>>>> response: " + respJson);
        return JSON.parseObject(respJson, typeReference);
    }

    static <T> T httpPost(String requestString, String url, Class<T> clazz) {
        log.debug(" =====>>>> httpPost: " + url);
        String respJson = Default.post(requestString, url);
        log.debug(" =====>>>> response: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

    static <T> T httpPost(String requestString, String url, TypeReference<T> typeReference) {
        log.debug(" =====>>>> httpPost: " + url);
        String respJson = Default.post(requestString, url);
        log.debug(" =====>>>> response: " + respJson);
        return JSON.parseObject(respJson, typeReference);
    }

}
