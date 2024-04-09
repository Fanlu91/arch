package com.flhai.myrpc.core.api;

public interface Filter {
    RpcResponse preFilter(RpcRequest request);

    RpcResponse postFilter(RpcRequest request, RpcResponse response);

    //    Filter next();
    Filter DEFAULT = new Filter() {
        @Override
        public RpcResponse preFilter(RpcRequest request) {
            return null;
        }

        @Override
        public RpcResponse postFilter(RpcRequest request, RpcResponse response) {
            return response;
        }
    };
}
