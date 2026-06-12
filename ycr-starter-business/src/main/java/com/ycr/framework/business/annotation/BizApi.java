package com.ycr.framework.business.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务接入点注解
 *
 * <p>标注在方法上，触发 {@code BizInterceptorChain}：所有 {@code BizInterceptor} 按序对该方法做
 * 前置/后置/异常处理。{@link #value()} 作为该接入点的名称，用于日志/审计标识。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BizApi {

    /**
     * 接入点名称（日志/审计标识），默认空
     */
    String value() default "";
}
