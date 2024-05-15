package com.flhai.myrpc.core.consumer;

import com.flhai.myrpc.core.api.*;
import com.flhai.myrpc.core.http.HttpInvoker;
import com.flhai.myrpc.core.http.OkHttpInvoker;
import com.flhai.myrpc.core.governance.SlidingTimeWindow;
import com.flhai.myrpc.core.meta.InstanceMeta;
import com.flhai.myrpc.core.util.MethodUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.flhai.myrpc.core.util.TypeUtils.castMethodReturnType;

@Slf4j
public class MyInvocationHandler implements InvocationHandler {

    Class<?> serviceClass;
    RpcContext rpcContext;
    List<InstanceMeta> providers;
    List<InstanceMeta> isolatedProviders;

    List<InstanceMeta> halfOpenProviders;
    HttpInvoker httpInvoker;

    Map<String, SlidingTimeWindow> windowMap = new HashMap<>();

    ScheduledExecutorService scheduledExecutorService;

    public MyInvocationHandler(Class<?> serviceClass, RpcContext rpcContext, List<InstanceMeta> providers) {
        this.serviceClass = serviceClass;
        this.rpcContext = rpcContext;
        this.providers = providers;
        int timeout = Integer.parseInt(rpcContext.getParameters().getOrDefault("consumer.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);

        this.isolatedProviders = new CopyOnWriteArrayList<>();
        this.halfOpenProviders = new ArrayList<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        int halfOpenInitialDelay = Integer.parseInt(rpcContext.getParameters().getOrDefault("app.halfOpenInitialDelay", "0"));
        int halfOpenDelay = Integer.parseInt(rpcContext.getParameters().getOrDefault("app.halfOpenDelay", "30"));
        this.scheduledExecutorService.scheduleWithFixedDelay(this::halfOpen, halfOpenInitialDelay, halfOpenDelay, TimeUnit.SECONDS);
    }

    private void halfOpen() {
        log.debug("half Open Providers: " + isolatedProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(serviceClass.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        int retry = Integer.parseInt(rpcContext.getParameters().getOrDefault("consumer.retries", "1"));
        while (retry-- > 0) {
            try {
                List<Filter> filters = rpcContext.getFilters();
                for (Filter filter : filters) {
                    Object preResult = filter.preFilter(rpcRequest);
                    if (preResult != null) {
                        log.debug(filter.getClass().getName() + " ==> prefilter: " + preResult);
                        return preResult;
                    }
                }
                InstanceMeta instance;
                boolean isAliveCheck = false;
//                log.debug("===> current providers: " + providers);
                synchronized (halfOpenProviders) {
                    if (halfOpenProviders.isEmpty()) {
                        List<InstanceMeta> route = rpcContext.getRouter().route(providers);
                        instance = rpcContext.getLoadBalancer().choose(route);
                    } else {
                        instance = halfOpenProviders.remove(0);
                        isAliveCheck = true;
                        log.debug("===> try half open instance: " + instance);
                    }
                }
                if (instance == null) {
                    log.error("===> no available instance");
                    throw new RpcException("no available instance");
                }
                String url = instance.toUrl();
                log.debug("===> choose url = " + url);

                RpcResponse rpcResponse = null;
                Object result = null;
                try {
                    rpcResponse = httpInvoker.post(rpcRequest, url);
                    // 这里cache filter应该放在最后一个执行，否则缓存的结果可能不是最终结果，造成问题
                    result = castReturnResult(method, rpcResponse);
                } catch (Exception ex) {
                    log.warn("invoke error: " + ex.getMessage());
                    if (isAliveCheck) {
                        isAliveCheck = false;
                        log.warn("===> instance " + instance + " is still not alive");
                    } else {
                        // 故障的规则统计和隔离
                        // 每一次异常，记录一次，统计30s的异常
                        SlidingTimeWindow window = windowMap.get(url);
                        if (window == null) {
                            window = new SlidingTimeWindow();
                            windowMap.put(url, window);
                        }
                        window.record(System.currentTimeMillis());
                        log.debug("===> instance url: " + url + ", in window with: " + window.getSum());
                        // 发生10次故障，就隔离
                        if (window.getSum() >= 10) {
                            boolean isolate = isolate(instance);
                            if (!isolate) {
                                log.error("instance " + instance + " isolate failed");
                            } else
                                log.info("instance " + instance + " is isolated");
                        }
                    }

                }


                // half open instance 探活成功
                if (isAliveCheck && rpcResponse != null && rpcResponse.isStatus()) {

                    boolean recover = recover(instance);
                    // 记录instance恢复 展示当前providers,isolatedProviders
                    if (!recover) {
                        log.error("instance " + instance + " recover failed");
                    } else
                        log.info("instance {} recover, providers: {}, isolatedProviders: {}", instance, providers, isolatedProviders);
                }

                for (Filter filter : filters) {
                    Object filterResult = filter.postFilter(rpcRequest, rpcResponse, result);
                    if (filterResult != null) {
                        return filterResult;
                    }
                }
                return result;
            } catch (Exception ex) {
                log.debug("retry left = " + retry);
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }
        return null;
    }

    private synchronized boolean isolate(InstanceMeta instance) {
        log.debug("===> isolate instance: " + instance);
        log.debug("from providers: " + providers);
        boolean remove = providers.remove(instance);
        if (!remove) {
            log.error("instance " + instance + " not in providers");
            return false;
        }

        log.debug("isolatedProviders: " + isolatedProviders);
        isolatedProviders.add(instance);
        return true;
    }

    private synchronized boolean recover(InstanceMeta instance) {
        log.debug("===> recover instance: " + instance);
        log.debug("from isolatedProviders: " + isolatedProviders);
        boolean remove = isolatedProviders.remove(instance);
        if (!remove) {
            log.error("instance " + instance + " not in isolatedProviders");
            return false;
        }

        log.debug("providers: " + providers);
        providers.add(instance);
        return true;
    }

    private static Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return castMethodReturnType(method, data);
        } else {
            RpcException exception = rpcResponse.getEx();
            if (exception != null) {
                log.error("response error: " + exception);
                throw exception;
            }
        }
        return null;
    }

}
