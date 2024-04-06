package com.flhai.myrpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class InstanceMeta {
    private String schema; // http
    private String host;
    private int port;
    private String context;

    private boolean isOnline;
    private Map<String, String> params;

    public InstanceMeta(String schema, String host, int port, String context, boolean isOnline) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
        this.isOnline = isOnline;
    }

    public InstanceMeta(String host, int port) {
        this.host = host;
        this.port = port;
        this.schema = "http";
    }

    public String toPath() {
        return String.format("%s://%s:%d", schema, host, port);
    }
}