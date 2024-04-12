package com.flhai.myrpc.core.util;

import com.flhai.myrpc.core.api.RpcException;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MockUtil {

    public static Object mock(Class type) {
        return mock(type, null);
    }

    /**
     * @param type        擦除泛型后的类型类
     * @param genericType 泛型类型，针对List<T>这种类型做处理
     * @return
     */
    public static Object mock(Class type, Type genericType) {
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return 1;
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return 10000L;
        }
        if (Number.class.isAssignableFrom(type)) {
            return 1;
        }
        if (type.equals(String.class)) {
            return "this_is_a_mock_string";
        }
        if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return true;
        }
        // 检查是否是数组类型
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            Object array = Array.newInstance(componentType, 1); // 创建一个包含一个元素的数组
            // 填充数组
            Array.set(array, 0, mock(componentType));
            return array;
        }
        // 检查是否是List类型
        if (type.isAssignableFrom(java.util.List.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
//            Class listType = (Class) parameterizedType.getRawType();
            Class itemType = (Class) parameterizedType.getActualTypeArguments()[0];
            return Arrays.asList(mock(itemType));
        }
        // 检查是否是Map类型
        if (type.isAssignableFrom(java.util.Map.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
//            Class mapType = (Class) parameterizedType.getRawType();
            Class keyType = (Class) parameterizedType.getActualTypeArguments()[0];
            Class valueType = (Class) parameterizedType.getActualTypeArguments()[1];
            Map<Object, Object> map = new HashMap<>();
            map.put(mock(keyType), mock(valueType));
            return map;
        }


        return mockPojo(type);
    }


    private static Object mockPojo(Class type) {
        Object result = null;
        try {
            result = type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RpcException(e);
        } catch (IllegalAccessException e) {
            throw new RpcException(e);
        } catch (InvocationTargetException e) {
            throw new RpcException(e);
        } catch (NoSuchMethodException e) {
            throw new RpcException(e);
        }
        Field[] fields = type.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            Class<?> fType = f.getType();
            Object fValue = mock(fType);
            try {
                f.set(result, fValue);
            } catch (IllegalAccessException e) {
                throw new RpcException(e);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(mock(UserDto.class));
    }

    public static class UserDto {
        private int a;
        private String b;

        @Override
        public String toString() {
            return a + "," + b;
        }
    }
}
