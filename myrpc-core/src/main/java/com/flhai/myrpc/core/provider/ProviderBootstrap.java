package com.flhai.myrpc.core.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.core.api.RegistryCenter;
import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.meta.ProviderMeta;
import com.flhai.myrpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;

    private InstanceMeta instance;
    RegistryCenter registryCenter;
    @Value("${server.port}")
    private String port;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    // 据说可以省略
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @PostConstruct // 相当于 init method
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(MyProvider.class);
        providers.values().forEach(provider -> {
            genInterfaces(provider);
        });
        registryCenter = applicationContext.getBean(RegistryCenter.class);

    }

    @SneakyThrows
    // 不放在init中因为这时spring还没有加载完成，服务可能不可用
    // 在ProviderConfig中调用
    public void start() {
        registryCenter.start();
        instance = new InstanceMeta(InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(port));
//        instance = InetAddress.getLocalHost().getHostAddress() + "_" + port;
        skeleton.keySet().forEach(this::registerProvider);
    }

    @PreDestroy
    public void stop() {
        System.out.println("===> ProviderBootstrap stop");
        skeleton.keySet().forEach(this::unregisterProvider);
        registryCenter.stop();
    }

    private void registerProvider(String serviceName) {
        registryCenter.register(serviceName, instance);
    }

    private void unregisterProvider(String serviceName) {
        registryCenter.unregister(serviceName, instance);
    }

    private void genInterfaces(Object provider) {
        Class<?>[] interfaces = provider.getClass().getInterfaces();
        Arrays.stream(interfaces).forEach(providerInterface -> {
            Method[] methods = providerInterface.getMethods();
            for (Method method : methods) {
                if (MethodUtils.checkLocalMethod(method)) {
                    continue;
                }
                createProvider(providerInterface, provider, method);
            }
        });
    }

    private void createProvider(Class<?> providerInterface, Object o, Method method) {
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setMethod(method);
        providerMeta.setSignName(MethodUtils.methodSign(method));
        providerMeta.setServiceImpl(o);
        System.out.println("Created provider, providerMeta = " + providerMeta);
        skeleton.add(providerInterface.getCanonicalName(), providerMeta);
    }


}