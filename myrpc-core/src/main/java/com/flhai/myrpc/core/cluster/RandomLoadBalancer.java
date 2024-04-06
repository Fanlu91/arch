package com.flhai.myrpc.core.cluster;

import com.flhai.myrpc.core.api.LoadBalancer;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer<InstanceMeta> implements LoadBalancer<com.flhai.myrpc.core.meta.InstanceMeta> {

    Random random = new Random();

    public com.flhai.myrpc.core.meta.InstanceMeta choose(List<com.flhai.myrpc.core.meta.InstanceMeta> providers) {
        if (providers == null || providers.size() == 0) {
            return null;
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
        return providers.get(random.nextInt(providers.size()));
    }
}
