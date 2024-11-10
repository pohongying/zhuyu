package com.luoyi.imcommon.annotation;

import com.luoyi.imcommon.enums.IMListenerType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 监听消息类型的注解
 */
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface IMListener {

    IMListenerType type();

}
