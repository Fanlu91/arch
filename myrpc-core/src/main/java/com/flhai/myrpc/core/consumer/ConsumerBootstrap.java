package com.flhai.myrpc.core.consumer;

import com.flhai.myrpc.core.annotation.MyConsumer;
import com.flhai.myrpc.core.api.LoadBalancer;
import com.flhai.myrpc.core.api.RegistryCenter;
import com.flhai.myrpc.core.api.Router;
import com.flhai.myrpc.core.api.RpcContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;
    Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Map<String, Object> stub = new HashMap<>();

    public void startApplication() {
        System.out.println("------------startApplication called");

        RpcContext rpcContext = new RpcContext();
        rpcContext.setRouter(applicationContext.getBean(Router.class));
        rpcContext.setLoadBalancer(applicationContext.getBean(LoadBalancer.class));

        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

//        String urls = environment.getProperty("myrpc.providers", "");
//        if (urls.isEmpty()) {
//            throw new RuntimeException("myrpc.providers is empty");
//        }
//        List<String> providers = List.of(urls.split(","));


        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanName);
            List<Field> annotatedFields = findAnnotatedField(bean.getClass());
            if (annotatedFields.size() > 0) {
                System.out.println("------------" + beanName + " has annotated fields");
                annotatedFields.stream().forEach(field -> {
                    Class<?> serviceClass = field.getType();
                    String serviceName = serviceClass.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
//                        consumer = createComsumer(serviceClass, rpcContext, providers);
                        consumer = createComsumerFromRegistry(serviceClass, rpcContext, registryCenter);
                    }

                    field.setAccessible(true);
                    try {
                        field.set(bean, consumer);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
//                    stub.put(serviceName,consumer);
                });
            }
        }
    }


    private Object createComsumer(Class<?> serviceClass, RpcContext context, List<String> providers) {
        System.out.println("------------createConsumer called");
        return Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MyInvocationHandler(serviceClass, context, providers));
    }

    private Object createComsumerFromRegistry(Class<?> serviceClass, RpcContext rpcContext, RegistryCenter registryCenter) {
        System.out.println("------------createConsumerFromRegistry called");
        String serviceName = serviceClass.getCanonicalName();
        List<String> providers = registryCenter.fetchAll(serviceName);
        return Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MyInvocationHandler(serviceClass, rpcContext, providers));
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
