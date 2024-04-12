package com.flhai.myrpc.core.registry.zk;

import com.flhai.myrpc.core.api.RegistryCenter;
import com.flhai.myrpc.core.api.RpcException;
import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.meta.ServiceMeta;
import com.flhai.myrpc.core.registry.ChangedListener;
import com.flhai.myrpc.core.registry.Event;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client = null;
    private TreeCache treeCache = null;

    @Value("${myrpc.zkServers}")
    private String zkServers;

    @Value("${myrpc.zkNamespace}")
    private String zkNamespace;

    @Override
    public void start() {
        log.info("---start zk registry client");
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkServers)
                .namespace(zkNamespace)
                .retryPolicy(retryPolicy)
                .build();
        log.info("===> start zk registry client" + zkServers + " " + zkNamespace);
        client.start();
    }

    @Override
    public void stop() {
        log.info("===> stop zk registry client");
        treeCache.close();
        client.close();
    }


    public void register(ServiceMeta serviceMeta, InstanceMeta instance) {
        String servicePath = "/" + serviceMeta.toPath();
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            String instancePath = servicePath + "/" + instance;
            log.info("===>register instance to zk : " + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta serviceMeta, InstanceMeta instance) {
        log.info("---unregister service to zk : " + serviceMeta + ", instance: " + instance);

        String servicePath = "/" + serviceMeta.toPath();
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }

            String instancePath = servicePath + "/" + instance;
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta serviceMeta) {
        log.info("---fetch all service from zk : " + serviceMeta.toPath());
        String servicePath = "/" + serviceMeta.toPath();
        try {
            List<String> nodes = client.getChildren().forPath(servicePath);
            // node = "InstanceMeta(schema=http, host=127.0.0.1, port=8080, context=null, isOnline=false, params=null)"
            List<InstanceMeta> instanceMetaList = mapInstanceMeta(nodes);
            return instanceMetaList;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @NotNull
    private static List<InstanceMeta> mapInstanceMeta(List<String> nodes) {
        return nodes.stream().map(node -> {
            String[] parts = node.split(",");
            String host = parts[1].split("=")[1];
            int port = Integer.parseInt(parts[2].split("=")[1]);
            return new InstanceMeta(host, port);
        }).collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public void subscribe(ServiceMeta serviceMeta, ChangedListener listener) {
        log.info("---subscribe service from zk : " + serviceMeta.toPath());
        // 监听service path 下的子节点变化
        // treeCache 是zk数据结构的一个本地缓存对象
        treeCache = TreeCache.newBuilder(client, "/" + serviceMeta.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        // client 是一个CuratorFramework实例，代表与ZooKeeper集群的连接。你可以使用这个客户端实例来执行更多的ZooKeeper操作，如查询节点数据。
        // event 是一个TreeCacheEvent实例，包含了事件的类型（如节点创建、节点删除、节点数据变化等）和与事件相关的数据（如变化的节点的路径和数据）。
        // TreeCacheEvent让监听器能够根据事件的具体情况执行相应的逻辑。
        treeCache.getListenable().addListener((client, event) -> {
            log.info("---zk service changed: " + event);
            List<InstanceMeta> nodes = fetchAll(serviceMeta);
            listener.fireChange(new Event(nodes));
        });
        treeCache.start();
    }
}
