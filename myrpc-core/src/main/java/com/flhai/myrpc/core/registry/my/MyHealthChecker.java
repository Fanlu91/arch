package com.flhai.myrpc.core.registry.my;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * health checker for registry
 */
@Slf4j
public class MyHealthChecker {

    ScheduledExecutorService consumerExecutorService = null;
    ScheduledExecutorService providerExecutorService = null;


    public void start() {
        log.info("==> start my registry client with servers");
        consumerExecutorService = Executors.newScheduledThreadPool(1);
        providerExecutorService = Executors.newScheduledThreadPool(1);
    }

    public void providerCheck(Callback callback) {
        providerExecutorService.scheduleAtFixedRate(() -> {
            log.debug("==> provider check");
            try {
                callback.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void consumerCheck(Callback callback) {
        consumerExecutorService.scheduleAtFixedRate(() -> {
            log.debug("==> consumer check");
            try {
                callback.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        gracefulShutdown(consumerExecutorService);
        gracefulShutdown(providerExecutorService);
    }

    private void gracefulShutdown(ScheduledExecutorService executorService) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public interface Callback {
        void call() throws Exception;
    }
}
