package com.flhai.myrpc.core.api;

import lombok.Data;

import java.util.List;

@Data
public class RpcContext {
    LoadBalancer loadBalancer;
    Router router;
    List<Filter> filters; // todo
}
