package com.ycr.framework.idempotent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 幂等注解 —— 防止接口重复提交
 *
 * <p>标注在方法上，由 {@code IdempotentAspect} 拦截：在 {@link #timeout()} 时间窗口内，相同幂等键的
 * 重复请求将被拒绝并返回统一响应。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等名称，留空则取「类名#方法名」
     */
    String name() default "";

    /**
     * 幂等键，支持 Spring EL（按方法入参求值），如 {@code "#orderId"} 实现按订单防重
     */
    String key() default "";

    /**
     * 幂等时间窗口
     */
    int timeout() default 1;

    /**
     * 时间窗口单位，默认秒
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 触发重复提交时的提示信息
     */
    String message() default "请勿重复操作";
}
