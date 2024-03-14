package com.flhai.myrpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {
    private String service; // interface com.flhai.myrpc.demo.api.UserService
    private String method; // findById
    private Object[] params; //
}
