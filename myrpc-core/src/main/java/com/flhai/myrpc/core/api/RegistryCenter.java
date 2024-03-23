package com.flhai.myrpc.core.api;

import java.util.List;

public interface RegistryCenter {
    void start();

    void stop();

    // provider
    void register(String serviceName, String serviceAddress);

    void unregister(String serviceName, String serviceAddress);

    // consumer

    List<String> fetchAll(String serviceName);

    static class StaticRegistryCenter implements RegistryCenter {

        List<String> serviceList;

        public StaticRegistryCenter(List<String> serviceList) {
            this.serviceList = serviceList;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void register(String serviceName, String serviceAddress) {
        }

        @Override
        public void unregister(String serviceName, String serviceAddress) {
        }

        @Override
        public List<String> fetchAll(String serviceName) {
            return serviceList;
        }
    }

}
