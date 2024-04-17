package com.flhai.myrpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RpcRequest {
    private String service; // interface com.flhai.myrpc.demo.api.UserService
    private String methodSign; // findById
    private Object[] args; //
    private Map<String, String> params = new HashMap<>();

}
