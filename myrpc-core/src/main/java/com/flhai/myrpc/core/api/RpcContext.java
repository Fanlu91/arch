package com.flhai.myrpc.core.api;

import com.flhai.myrpc.core.config.ConsumerProperties;
import com.flhai.myrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RpcContext {
    LoadBalancer<InstanceMeta> loadBalancer;
    Router router;
    List<Filter> filters;
    private Map<String, String> parameters = new HashMap<>();
    private ConsumerProperties consumerProperties;

    /**
     * 线程独立的上下文参数
     */
    public static ThreadLocal<Map<String, String>> ContextParameters = new ThreadLocal<>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<>();
        }
    };

    public static void setContextParameter(String key, String value) {
        ContextParameters.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return ContextParameters.get().get(key);
    }

    public static void removeContextParameter(String key) {
        ContextParameters.get().remove(key);
    }
}
