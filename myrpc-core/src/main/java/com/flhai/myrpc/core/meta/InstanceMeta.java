package com.flhai.myrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述服务实例元数据
 */
@Data
@AllArgsConstructor
public class InstanceMeta {
    private String schema; // http
    private String host;
    private int port;
    private String context;
    private boolean status;
    private Map<String, String> parameters;

    public InstanceMeta(String schema, String host, int port, String context, boolean isOnline) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
        this.status = isOnline;
        this.parameters = new HashMap<>();
    }

    /**
     * http instance
     *
     * @param host
     * @param port
     */
    public InstanceMeta(String host, int port) {
        this.host = host;
        this.port = port;
        this.schema = "http";
        this.context = "myrpc";
        this.status = true;
        this.parameters = new HashMap<>();
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }

    public String toZkPath() {
        return String.format("%s:%d", host, port);
    }

    public String toMetaString() {
        return JSON.toJSONString(this);
    }

    public String paramsToJson() {
        return JSON.toJSONString(this.getParameters());
    }
}
