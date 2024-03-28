package com.flhai.myrpc.core.api;

import java.util.List;

public interface RegistryCenter {
    void start(); // p/c

    void stop(); // p/c

    // provider
    void register(String serviceName, String serviceAddress); // p

    void unregister(String serviceName, String serviceAddress); // p

    // consumer

    List<String> fetchAll(String serviceName); // c
//    void subscribe(String serviceName, NotifyListener listener); // c
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
