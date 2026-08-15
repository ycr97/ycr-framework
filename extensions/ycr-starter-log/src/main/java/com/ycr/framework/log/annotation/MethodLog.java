package com.ycr.framework.log.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法调用日志注解（开发排障型）
 *
 * <p>标注方法或类，由 {@code MethodLogAspect} 把入参/出参/耗时/异常打到 SLF4J。
 * 与审计 {@code @Log} 彻底分离：不落库、纯打印、受日志级别门控。</p>
 *
 * @author ycr
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodLog {

    /** 描述，空则取方法名 */
    String value() default "";

    /** 是否打印入参 */
    boolean args() default true;

    /** 是否打印出参 */
    boolean result() default true;
}
