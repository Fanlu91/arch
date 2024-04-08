package com.flhai.myrpc.core.meta;

import lombok.Builder;
import lombok.Data;

/**
 * 描述服务元数据
 */
@Data
@Builder
public class ServiceMeta {
    private String app;
    private String namespace;
    private String env;
    private String name;

    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }
}
