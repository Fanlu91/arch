//package com.flhai.myrpc.core.config;
//
//import com.ctrip.framework.apollo.model.ConfigChange;
//import com.ctrip.framework.apollo.model.ConfigChangeEvent;
//import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
//import org.springframework.context.ApplicationContext;
//
//@Slf4j
//public class ApolloChangedListener {
//    @Autowired
//    ApplicationContext applicationContext;
//
//    @ApolloConfigChangeListener("${apollo.bootstrap.namespaces}")
//    private void changeHandler(ConfigChangeEvent changeEvent){
//        for (String key : changeEvent.changedKeys()) {
//            ConfigChange change = changeEvent.getChange(key);
//            log.info("Found change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
//                    change.getPropertyName(), change.getOldValue(), change.getNewValue(), change.getChangeType());
//        }
//
//        // refresh beans
//        // 更新相应bean属性的值，主要是存在@ConfigurationProperties注解的bean
//        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
//    }
//}
