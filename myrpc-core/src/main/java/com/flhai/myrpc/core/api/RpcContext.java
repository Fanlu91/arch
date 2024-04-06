package com.flhai.myrpc.core.api;

import com.flhai.myrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.List;

@Data
public class RpcContext {
    LoadBalancer<InstanceMeta> loadBalancer;
    Router router;
    List<Filter> filters; // todo
}
