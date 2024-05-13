package com.flhai.myrpc.core.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.core.registry.RegistryCenter;
import com.flhai.myrpc.core.config.AppProperties;
import com.flhai.myrpc.core.config.ProviderProperties;
import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.meta.ProviderMeta;
import com.flhai.myrpc.core.meta.ServiceMeta;
import com.flhai.myrpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;
    private AppProperties appProperties;
    private ProviderProperties providerProperties;

    private String port;
    RegistryCenter registryCenter;
    private InstanceMeta instance;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    public ProviderBootstrap(String port, AppProperties appProperties,
                             ProviderProperties providerProperties) {
        this.port = port;
        this.appProperties = appProperties;
        this.providerProperties = providerProperties;
    }

    // 据说可以省略
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @PostConstruct // 相当于 init method
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(MyProvider.class);
        providers.values().forEach(this::genInterfaces);
        registryCenter = applicationContext.getBean(RegistryCenter.class);
    }

    @SneakyThrows
    // 不放在init中因为这时spring还没有加载完成，服务可能不可用
    // 在ProviderConfig中调用
    public void start() {
        registryCenter.start();
        instance = new InstanceMeta(InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(port));
        instance.getParams().putAll(providerProperties.getMetas());
        skeleton.keySet().forEach(this::registerProvider);
    }

    @PreDestroy
    public void stop() {
        log.info("===> ProviderBootstrap stop");
        skeleton.keySet().forEach(this::unregisterProvider);
        registryCenter.stop();
    }

    private void registerProvider(String serviceName) {
        ServiceMeta serviceMeta = ServiceMeta
                .builder()
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .env(appProperties.getEnv())
                .name(serviceName).build();
        registryCenter.register(serviceMeta, instance);
    }

    private void unregisterProvider(String serviceName) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .env(appProperties.getEnv())
                .name(serviceName).build();
        registryCenter.unregister(serviceMeta, instance);
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
        log.info("Created provider, providerMeta = " + providerMeta);
        skeleton.add(providerInterface.getCanonicalName(), providerMeta);
    }


}