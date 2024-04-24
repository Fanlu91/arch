package com.flhai.myrpc.core.provider;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TokenBucketLimiter {
    private Map<String, AtomicInteger> limiters = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutorService;
    private final int refillInterval;  // 毫秒
    private final int maxTokens;  // 最大令牌数

    public TokenBucketLimiter(int refillInterval, int maxTokens) {
        this.refillInterval = refillInterval;
        this.maxTokens = maxTokens;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        startRefill();
    }

    private void startRefill() {
        this.scheduledExecutorService.scheduleWithFixedDelay(this::refillTokens, 0, refillInterval, TimeUnit.MILLISECONDS);
    }

    private void refillTokens() {
        log.debug("Refilling tokens");
        limiters.forEach((key, limiter) -> {
            log.debug("Refilling tokens for key: {}, current token left: {}", key, limiter.get());
            limiter.set(maxTokens);
        });
    }

    public boolean tryAcquire(String key) {
        return limiters.computeIfAbsent(key, k -> new AtomicInteger(maxTokens)).getAndDecrement() > 0;
    }

    @PreDestroy
    public void stop() {
        log.debug("Shutting down token bucket limiter");
        scheduledExecutorService.shutdown();
    }

}
