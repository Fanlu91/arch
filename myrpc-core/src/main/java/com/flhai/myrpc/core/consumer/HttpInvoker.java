package com.flhai.myrpc.core.consumer;

import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;

public interface HttpInvoker {
    public RpcResponse<?> post(RpcRequest rpcRequest, String url);
}
