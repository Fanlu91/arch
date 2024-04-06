package com.flhai.myrpc.core.cluster;

import com.flhai.myrpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRibonLoadBalancer<InstanceMeta> implements LoadBalancer<InstanceMeta> {
    private AtomicInteger atomicInteger = new AtomicInteger(0);


    public InstanceMeta choose(List<InstanceMeta> providers) {
        if (providers == null || providers.size() == 0) {
            return null;
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
        int index = atomicInteger.getAndIncrement() % providers.size();
        return providers.get(index);
    }
}
