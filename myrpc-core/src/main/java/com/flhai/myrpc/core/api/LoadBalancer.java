package com.flhai.myrpc.core.api;

import java.util.List;

/**
 * 负载均衡
 * 随机、轮询
 * 权重 weighted
 * 自适应 Adaptive，根据调用次数、调用时间等动态调整
 */
public interface LoadBalancer<InstanceMeta> {
    InstanceMeta choose(List<InstanceMeta> providers);

    LoadBalancer Default = providers -> (providers == null || providers.size() == 0) ? null : providers.get(0);
}
