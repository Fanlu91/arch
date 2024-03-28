package com.flhai.myrpc.core.registry;

import com.flhai.myrpc.core.api.RegistryCenter;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.stream.Collectors;

public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client = null;
    private TreeCache treeCache = null;

    @Override
    public void start() {
        System.out.println("---start zk registry client");
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .namespace("myrpc")
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    @Override
    public void stop() {
        System.out.println("===> stop zk registry client");
        treeCache.close();
        client.close();
    }


    public void register(String serviceName, String instanceName) {
        System.out.println("---register service to zk : " + serviceName + ", instance: " + instanceName);
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
        System.out.println("---unregister service to zk : " + serviceName + ", instance: " + instanceName);

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
        System.out.println("---fetch all service from zk : " + serviceName);
        String servicePath = "/" + serviceName;
        try {
            List<String> nodes = client.getChildren().forPath(servicePath);
            List<String> urls = nodes.stream()
                    .map(node -> "http://" + node.replace("_", ":"))
                    .collect(Collectors.toList());
            return urls;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SneakyThrows
    public void subscribe(String serviceName, ChangedListener listener) {
        System.out.println("---subscribe service from zk : " + serviceName);
        // 监听service path 下的子节点变化
        // treeCache 是zk数据结构的一个本地缓存对象
        treeCache = TreeCache.newBuilder(client, "/" + serviceName)
                .setCacheData(true).setMaxDepth(2).build();
        // client 是一个CuratorFramework实例，代表与ZooKeeper集群的连接。你可以使用这个客户端实例来执行更多的ZooKeeper操作，如查询节点数据。
        // event 是一个TreeCacheEvent实例，包含了事件的类型（如节点创建、节点删除、节点数据变化等）和与事件相关的数据（如变化的节点的路径和数据）。
        // TreeCacheEvent让监听器能够根据事件的具体情况执行相应的逻辑。
        treeCache.getListenable().addListener((client, event) -> {
            System.out.println("---zk service changed: " + event);
            List<String> nodes = fetchAll(serviceName);
            listener.fireChange(new Event(nodes));
        });
        treeCache.start();
    }
}
