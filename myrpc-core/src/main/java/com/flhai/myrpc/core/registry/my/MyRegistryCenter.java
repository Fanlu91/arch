package com.flhai.myrpc.core.registry.my;

import com.alibaba.fastjson.TypeReference;
import com.flhai.myrpc.core.consumer.HttpInvoker;
import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.meta.ServiceMeta;
import com.flhai.myrpc.core.registry.ChangedListener;
import com.flhai.myrpc.core.registry.Event;
import com.flhai.myrpc.core.registry.RegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MyRegistryCenter implements RegistryCenter {

    @Value("${myregistry.servers:}")
    private String servers;

    Map<String, Long> VERSIONS = new HashMap<>();
    ScheduledExecutorService scheduledExecutorService = null;

    @Override
    public void start() {
        log.info("==> start my registry client with servers: {}", servers);
        scheduledExecutorService = Executors.newScheduledThreadPool(1);

    }

    @Override
    public void stop() {
        log.info("==> stop my registry client with servers: {}", servers);
        scheduledExecutorService.shutdown();
        try {
            scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
            if (!scheduledExecutorService.isTerminated()) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceMeta serviceName, InstanceMeta instance) {
        log.info("==> register service: {} instance: {}", serviceName, instance);
        HttpInvoker.httpPost(instance.toMetaString(), servers + "/register?service=" + serviceName.toPath(), InstanceMeta.class);
    }

    @Override
    public void unregister(ServiceMeta serviceName, InstanceMeta instance) {
        log.info("==> unregister service: {} instance: {}", serviceName, instance);
        HttpInvoker.httpPost(instance.toMetaString(), servers + "/unregister?service=" + serviceName.toPath(), InstanceMeta.class);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta serviceName) {
        log.info("==> fetch all instances of service: {}", serviceName);
        List<InstanceMeta> instanceMetaList = HttpInvoker.httpGet(servers + "/findAll?service=" + serviceName.toPath(), new TypeReference<List<InstanceMeta>>() {
        });
        log.info("==> fetch all instances of service: {} result: {}", serviceName, instanceMetaList);
        return instanceMetaList;
    }


    @Override
    public void subscribe(ServiceMeta serviceName, ChangedListener listener) {
        log.info("==> subscribe service: {}", serviceName);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Long version = VERSIONS.getOrDefault(serviceName.toPath(), -1L);
            Long remoteVersion = HttpInvoker.httpGet(servers + "/version?service=" + serviceName.toPath(), Long.class);
            if (remoteVersion > version) {
                List<InstanceMeta> instanceMetaList = fetchAll(serviceName);
                log.debug("==> service: {} changed, old version: {}, new version: {}, instances: {}", serviceName, version, remoteVersion, instanceMetaList);
                listener.fireChange(new Event(instanceMetaList));
                VERSIONS.put(serviceName.toPath(), remoteVersion);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }
}
