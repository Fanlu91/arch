package com.flhai.myrpc.core.registry.my;

import com.alibaba.fastjson.TypeReference;
import com.flhai.myrpc.core.http.HttpInvoker;
import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.meta.ServiceMeta;
import com.flhai.myrpc.core.registry.ChangedListener;
import com.flhai.myrpc.core.registry.Event;
import com.flhai.myrpc.core.registry.RegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MyRegistryCenter implements RegistryCenter {

    @Value("${myregistry.servers:}")
    private String servers;
    Map<String, Long> VERSIONS = new HashMap<>();
    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    MyHealthChecker healthChecker = new MyHealthChecker();

    @Override
    public void start() {
        log.info("==> start my registry client with servers: {}", servers);
        healthChecker.start();
        healthChecker.providerCheck(this::providerRenew);
    }

    @Override
    public void stop() {
        log.info("==> stop my registry client with servers: {}", servers);
        healthChecker.stop();
    }

    public void providerRenew() {
        RENEWS.forEach((instance, serviceNames) -> {
            log.debug("==> renew instance: {} for {} services", instance.toUrl(), serviceNames.size());
            try {
                String servicesAsString = String.join((CharSequence) ",", serviceNames.stream().map(x -> x.toPath()).collect(Collectors.toList()));
                if (servicesAsString.endsWith(",")) {
                    servicesAsString = servicesAsString.substring(0, servicesAsString.length() - 1);
                }
                HttpInvoker.httpPost(instance.toMetaString(), servers + "/renews?services=" + servicesAsString, String.class);
            } catch (Exception e) {
                log.error("==> renew instance: {} for {} services failed", instance.toUrl(), serviceNames.size(), e);
            }
        });
    }


    @Override
    public void register(ServiceMeta serviceName, InstanceMeta instance) {
        log.info("==> register service: {} instance: {}", serviceName.toPath(), instance);
        HttpInvoker.httpPost(instance.toMetaString(), servers + "/register?service=" + serviceName.toPath(), InstanceMeta.class);
        RENEWS.add(instance, serviceName);
    }

    @Override
    public void unregister(ServiceMeta serviceName, InstanceMeta instance) {
        log.info("==> unregister service: {} instance: {}", serviceName, instance);
        HttpInvoker.httpPost(instance.toMetaString(), servers + "/unregister?service=" + serviceName.toPath(), InstanceMeta.class);
        RENEWS.remove(instance, serviceName);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta serviceMeta) {
        log.info("==> fetch all instances of service: {}", serviceMeta.toPath());
        List<InstanceMeta> instanceMetaList = HttpInvoker.httpGet(servers + "/findAll?service=" + serviceMeta.toPath(), new TypeReference<List<InstanceMeta>>() {
        });
        log.info("==> fetch all instances of service: {} result: {}", serviceMeta, instanceMetaList);
        return instanceMetaList;
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, ChangedListener listener) {
        log.info("==> subscribe service: {}", serviceMeta.toPath());
        healthChecker.consumerCheck(() -> consumerRenew(serviceMeta, listener));
    }

    public void consumerRenew(ServiceMeta serviceMeta, ChangedListener listener) {
        Long version = VERSIONS.getOrDefault(serviceMeta.toPath(), -1L);
        Long remoteVersion = HttpInvoker.httpGet(servers + "/version?service=" + serviceMeta.toPath(), Long.class);
        if (remoteVersion > version) {
            List<InstanceMeta> instanceMetaList = fetchAll(serviceMeta);
            log.info("==> service: {} changed, old version: {}, new version: {}, instances: {}", serviceMeta, version, remoteVersion, instanceMetaList);
            listener.fireChange(new Event(instanceMetaList));
            VERSIONS.put(serviceMeta.toPath(), remoteVersion);
        }
    }
}
