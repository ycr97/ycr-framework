package com.ycr.framework.ratelimiter.annotation;

import com.ycr.framework.ratelimiter.enums.LimitType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解 —— 基于 Redisson 令牌桶
 *
 * <p>标注在方法上，由 {@code RateLimiterAspect} 拦截：在 {@link #interval()} 时间内最多放行 {@link #rate()}
 * 次，超出抛限流异常并返回统一响应。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {

    /**
     * 限流类型
     */
    LimitType type() default LimitType.DEFAULT;

    /**
     * 限流名称，留空则取「类名#方法名」
     */
    String name() default "";

    /**
     * 限流键，支持 Spring EL（按方法入参求值），如 {@code "#userId"} 实现按用户独立限流
     */
    String key() default "";

    /**
     * 速率：{@link #interval()} 时间内产生的令牌数
     */
    int rate() default Integer.MAX_VALUE;

    /**
     * 速率间隔
     */
    int interval() default 0;

    /**
     * 速率间隔时间单位，默认秒
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 触发限流时的提示信息
     */
    String message() default "操作过于频繁，请稍后再试";
}
