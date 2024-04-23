package com.flhai.myrpc.core.annotation;

import com.flhai.myrpc.core.consumer.ConsumerConfig;
import com.flhai.myrpc.core.provider.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableMyRpc {
}
