package com.flhai.myrpc.core.registry;

import com.flhai.myrpc.core.api.RegistryCenter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client = null;

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .namespace("myrpc")
                .retryPolicy(retryPolicy)
                .build();

    }

    @Override
    public void stop() {
        client.close();
    }


    public void register(String serviceName, String instanceName) {
        String servicePath = "/" + serviceName;
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }

            String instancePath = servicePath + "/" + instanceName;
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(String serviceName, String instanceName) {
        String servicePath = "/" + serviceName;
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }

            String instancePath = servicePath + "/" + instanceName;
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> fetchAll(String serviceName) {
        return null;
    }
}
