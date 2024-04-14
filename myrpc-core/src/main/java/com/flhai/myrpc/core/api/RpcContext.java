package com.flhai.myrpc.core.api;

import com.flhai.myrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RpcContext {
    LoadBalancer<InstanceMeta> loadBalancer;
    Router router;
    List<Filter> filters;
    private Map<String, String> parameters = new HashMap<>();
}
