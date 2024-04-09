package com.flhai.myrpc.core.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class TypeUtils {

    public static Object castMethodReturnType(Method method, Object data) {
//        log.debug("data = " + data);
//        log.debug("method.getReturnType() = " + method.getReturnType());
//        log.debug("method.getGenericReturnType = " + method.getGenericReturnType());
        if (data instanceof JSONObject jsonResult) {
            log.debug("jsonResult = " + jsonResult);
            return jsonResult.toJavaObject(method.getGenericReturnType());
        } else if (data instanceof JSONArray jsonArray) {
            log.debug("jsonArray = " + jsonArray);
            return jsonArray.toJavaObject(method.getGenericReturnType());
        } else {
            log.debug("cast data = " + data);
            return cast(data, method.getReturnType());
        }
    }

    public static Object cast(Object origin, Class<?> type) {
        if (origin == null) {
            return null;
        }
//        log.debug("origin = " + origin);
        Class<?> originClass = origin.getClass();
        // 如果原始对象可以直接赋值给目标类型，则直接返回原始对象
        if (type.isAssignableFrom(originClass)) {
            return origin;
        }

        // 如果原始对象是HashMap且目标类型不是数组，使用fastjson将map转换为目标对象
        if (origin instanceof HashMap && !type.isArray()) {
            JSONObject jsonObject = new JSONObject((HashMap) origin);
            return jsonObject.toJavaObject(type);
        }

        // 处理目标类型为数组的情况
        // 处理数组类型的转换
        if (type.isArray()) {
            JSONArray jsonArray = null;
            // 将List转换为JSONArray
            if (origin instanceof List) {
                jsonArray = new JSONArray((List) origin);
            }
            // 如果origin本身就是JSONArray
            else if (origin instanceof JSONArray) {
                jsonArray = (JSONArray) origin;
            }
            // 将String转换为JSONArray
            else if (origin instanceof String) {
                jsonArray = JSONArray.parseArray((String) origin);
            }

            if (jsonArray != null) {
                // 对原始类型数组进行特殊处理
                if (type.getComponentType().isPrimitive()) {
                    Object array = Array.newInstance(type.getComponentType(), jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Array.set(array, i, jsonArray.get(i));
                    }
                    return array;
                } else {
                    // 对象类型数组可以直接使用toArray转换
//                    return jsonArray.toArray((Object[]) Array.newInstance(type.getComponentType(), jsonArray.size()));
                    // 创建一个指定类型的数组实例
                    Object array = Array.newInstance(type.getComponentType(), jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        // 使用toJavaObject转换每个元素，确保类型匹配
                        Array.set(array, i, jsonArray.getJSONObject(i).toJavaObject(type.getComponentType()));
                    }
                    return array;
                }
            }
        }

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(origin.toString());
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(origin.toString());
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(origin.toString());
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(origin.toString());
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(origin.toString());
        } else if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(origin.toString());
        } else if (type == short.class || type == Short.class) {
            return Short.parseShort(origin.toString());
        } else if (type == char.class || type == Character.class) {
            return origin.toString().charAt(0);
        } else if (type == String.class) {
            return origin.toString();
        } else {
            throw new RuntimeException("not support type");
        }

    }
}
