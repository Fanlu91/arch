package com.flhai.myrpc.core.api;

import lombok.Data;

@Data
public class RpcRequest {
    private String service; // interface com.flhai.myrpc.demo.api.UserService
    private String method; // findById
    private Object[] params; //
}
