package com.flhai.myrpc.core.registry;


import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.meta.ServiceMeta;

import java.util.List;

public interface RegistryCenter {
    void start(); // p/c

    void stop(); // p/c

    // provider
    void register(ServiceMeta serviceName, InstanceMeta instance); // p

    void unregister(ServiceMeta serviceName, InstanceMeta instance); // p

    // consumer

    List<InstanceMeta> fetchAll(ServiceMeta serviceName); // c

    void subscribe(ServiceMeta serviceName, ChangedListener listener); // c

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
        public void register(ServiceMeta serviceName, InstanceMeta instance) {
        }

        @Override
        public void unregister(ServiceMeta serviceName, InstanceMeta instance) {
        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta serviceName) {
            return serviceList;
        }

        @Override
        public void subscribe(ServiceMeta serviceName, ChangedListener listener) {

        }
    }

}
