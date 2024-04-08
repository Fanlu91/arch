package com.flhai.myrpc.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * 描述provider元数据，映射关系
 */
@Data
public class ProviderMeta {
    Method method;
    String signName;
    Object serviceImpl;
}
