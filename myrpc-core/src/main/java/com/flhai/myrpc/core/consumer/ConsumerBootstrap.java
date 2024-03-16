package com.flhai.myrpc.core.consumer;

import com.flhai.myrpc.core.annotation.MyConsumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Map<String, Object> stub = new HashMap<>();

    public void setApplicationContext() {
        System.out.println("------------setApplicationContext called");
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanName);
            List<Field> annotatedFields = findAnnotatedField(bean.getClass());
            if(annotatedFields.size() > 0) {
                System.out.println("------------"+beanName + " has annotated fields");
                annotatedFields.stream().forEach(field -> {
                    Class<?> serviceClass = field.getType();
                    String serviceName = serviceClass.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = createComsumer(serviceClass);
                    }
                    field.setAccessible(true);
                    try {
                        field.set(bean,consumer);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
//                    stub.put(serviceName,consumer);
                });
            }
        }
    }

    private Object createComsumer(Class<?> serviceClass) {
        System.out.println("------------createComsumer called");
        return Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[]{serviceClass}, new MyInvocationHandler(serviceClass));
    }

    private List<Field> findAnnotatedField(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(MyConsumer.class)) {
                    result.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return result;
    }
}
