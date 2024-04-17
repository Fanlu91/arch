package com.flhai.myrpc.core.filter;

import com.flhai.myrpc.core.api.Filter;
import com.flhai.myrpc.core.api.RpcContext;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;

import java.util.Map;

/**
 * 处理上下文参数
 */
public class ParameterFilter implements Filter {
    @Override
    public Object preFilter(RpcRequest request) {
        Map<String, String> params = RpcContext.ContextParameters.get();
        if (!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        // RpcContext.ContextParameters.get().clear();
        return null;
    }
}
