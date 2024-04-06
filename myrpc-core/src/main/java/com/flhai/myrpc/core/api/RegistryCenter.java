package com.flhai.myrpc.core.api;


import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.registry.ChangedListener;

import java.util.List;

public interface RegistryCenter {
    void start(); // p/c

    void stop(); // p/c

    // provider
    void register(String serviceName, InstanceMeta instance); // p

    void unregister(String serviceName, InstanceMeta instance); // p

    // consumer

    List<InstanceMeta> fetchAll(String serviceName); // c

    void subscribe(String serviceName, ChangedListener listener); // c

    static class StaticRegistryCenter implements RegistryCenter {

        List<InstanceMeta> serviceList;

        public StaticRegistryCenter(List<InstanceMeta> serviceList) {
            this.serviceList = serviceList;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void register(String serviceName, InstanceMeta instance) {
        }

        @Override
        public void unregister(String serviceName, InstanceMeta instance) {
        }

        @Override
        public List<InstanceMeta> fetchAll(String serviceName) {
            return serviceList;
        }

        @Override
        public void subscribe(String serviceName, ChangedListener listener) {

        }
    }

}
