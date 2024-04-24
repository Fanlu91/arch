package com.flhai.myrpc.core.annotation;

import com.flhai.myrpc.core.config.ConsumerConfig;
import com.flhai.myrpc.core.config.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableMyRpc {
}
