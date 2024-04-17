package com.flhai.myrpc.core.consumer;

import com.flhai.myrpc.core.annotation.MyConsumer;
import com.flhai.myrpc.core.api.*;
import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.meta.ServiceMeta;
import com.flhai.myrpc.core.registry.ChangedListener;
import com.flhai.myrpc.core.registry.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.flhai.myrpc.core.util.MethodUtils.findAnnotatedField;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;
    Environment environment;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    @Value("${app.retry.max}")
    private int retries;

    @Value("${app.timeout.default}")
    private int timeout;

    private Map<String, Object> stub = new HashMap<>();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void startApplication() {
        log.info("===> startApplication called");

        RpcContext rpcContext = new RpcContext();
        rpcContext.setRouter(applicationContext.getBean(Router.class));
        rpcContext.setLoadBalancer(applicationContext.getBean(LoadBalancer.class));
        rpcContext.setFilters(applicationContext.getBeansOfType(Filter.class).values().stream().toList());
        rpcContext.getParameters().put("retries", String.valueOf(retries));
        rpcContext.getParameters().put("timeout", String.valueOf(timeout));

        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanName);
            List<Field> annotatedFields = findAnnotatedField(bean.getClass(), MyConsumer.class);
            if (annotatedFields.size() > 0) {
                log.info("===>" + beanName + " has annotated fields");
                annotatedFields.stream().forEach(field -> {
                    Class<?> serviceClass = field.getType();
                    String serviceName = serviceClass.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
//                        consumer = createComsumer(serviceClass, rpcContext, providers);
                        consumer = createConsumerFromRegistry(serviceClass, rpcContext, registryCenter);
                    }

                    field.setAccessible(true);
                    try {
                        field.set(bean, consumer);
                    } catch (IllegalAccessException e) {
                        throw new RpcException(e);
                    }
                    stub.put(serviceName, consumer);
                });
            }
        }
    }

    private Object createConsumerFromRegistry(Class<?> serviceClass, RpcContext rpcContext, RegistryCenter registryCenter) {
        log.info("===> createConsumerFromRegistry called");
        String serviceName = serviceClass.getCanonicalName();
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app)
                .namespace(namespace)
                .env(env)
                .name(serviceName)
                .build();
        List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
        log.info("===> providers fetched from registry:");
        providers.forEach(System.out::println);

        if (providers.isEmpty()) {
            throw new RpcException("no provider found for service: " + serviceName);
        }

        registryCenter.subscribe(serviceMeta, new ChangedListener() {
            @Override
            public void fireChange(Event event) {
                providers.clear();
                providers.addAll(event.getInstance());
            }
        });
        //上面代码的 lambda 简化写法如下，我觉得可读性有所降低
        /*registryCenter.subscribe(serviceName, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });*/
        return createConsumer(serviceClass, rpcContext, providers);
    }

    private Object createConsumer(Class<?> serviceClass, RpcContext context, List<InstanceMeta> instance) {
        return Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MyInvocationHandler(serviceClass, context, instance));
    }


}
