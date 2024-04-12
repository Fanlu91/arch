package com.flhai.myrpc.core.api;

import lombok.Data;

@Data
public class RpcException extends RuntimeException {

    private String errorCode;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(Throwable cause, String errcode) {
        super(cause);
        this.errorCode = errcode;
    }

    // X => 技术类异常：
    // Y => 业务类异常：
    // Z => unknown, 搞不清楚
    public static final String SocketTimeoutEx = "X001" + "-" + "http_invoke_timeout";
    public static final String NoSuchMethodEx = "X002" + "-" + "method_not_exists";
    public static final String UnknownEx = "Z001" + "-" + "unknown";

}