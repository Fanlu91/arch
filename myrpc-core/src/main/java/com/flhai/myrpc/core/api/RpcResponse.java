package com.flhai.myrpc.core.api;

import lombok.Data;

@Data
public class RpcResponse<T> {
    boolean status;
    T data;
}
