package com.flhai.myrpc.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodUtils {
    public static boolean checkLocalMethod(final String method) {
        //本地方法不代理
        if ("toString".equals(method) ||
                "hashCode".equals(method) ||
                "notifyAll".equals(method) ||
                "equals".equals(method) ||
                "wait".equals(method) ||
                "getClass".equals(method) ||
                "notify".equals(method)) {
            return true;
        }
        return false;
    }

    public static boolean checkLocalMethod(final Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

    public static String methodSign(Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                c -> sb.append("_").append(c.getCanonicalName())
        );
        return sb.toString();
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(
                m -> System.out.println(methodSign(m))
        );
    }

    public static boolean isCompatibleType(Class<?> methodParamType, Class<?> paramType) {
        if (methodParamType.isAssignableFrom(paramType)) {
            return true; // 直接兼容
        } else if (methodParamType.isPrimitive()) {
            return isPrimitiveWrapperTypeCompatible(methodParamType, paramType); // 处理基本类型与包装类
        } else {
            return false;
        }
    }

    private static boolean isPrimitiveWrapperTypeCompatible(Class<?> primitiveType, Class<?> wrapperType) {
        if (primitiveType.equals(boolean.class)) return wrapperType.equals(Boolean.class);
        else if (primitiveType.equals(byte.class)) return wrapperType.equals(Byte.class);
        else if (primitiveType.equals(char.class)) return wrapperType.equals(Character.class);
        else if (primitiveType.equals(double.class)) return wrapperType.equals(Double.class);
        else if (primitiveType.equals(float.class)) return wrapperType.equals(Float.class);
        else if (primitiveType.equals(int.class)) return wrapperType.equals(Integer.class);
        else if (primitiveType.equals(long.class)) return wrapperType.equals(Long.class);
        else if (primitiveType.equals(short.class)) return wrapperType.equals(Short.class);
        // No need for else, as all cases are covered
        return false;
    }

    public static List<Field> findAnnotatedField(Class<?> clazz,Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(annotationClass)) {
                    result.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return result;
    }
}
