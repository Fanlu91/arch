package com.flhai.myrpc.core.cluster;

import com.flhai.myrpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRibonLoadBalancer<T> implements LoadBalancer<T> {
    private AtomicInteger index = new AtomicInteger(0);

    public T choose(List<T> providers) {
        if (providers == null || providers.size() == 0) {
            return null;
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }

        return providers.get(
                index.getAndIncrement() & Integer.MAX_VALUE  // 处理加到头出现负数的情况
                        % providers.size());
    }
}
