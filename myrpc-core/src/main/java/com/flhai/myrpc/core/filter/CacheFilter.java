package com.flhai.myrpc.core.filter;

import com.flhai.myrpc.core.api.Filter;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存过滤器
 * 用于缓存请求结果
 * 如果有多个过滤器，需要注意过滤器的执行顺序
 * CacheFilter应该放在最后一个执行 todo
 */
public class CacheFilter implements Filter {
    // 优化 guava cache，容量和过期时间
    Map<String, RpcResponse> cache = new ConcurrentHashMap<>();

    @Override
    public RpcResponse preFilter(RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public RpcResponse postFilter(RpcRequest request, RpcResponse response) {
        cache.putIfAbsent(request.toString(), response);
        return response;
    }
}
