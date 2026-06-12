package com.ycr.framework.sdk.annotation;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 SDK 客户端扫描
 *
 * <p>组合 {@link EnableFeignClients}，用 {@link AliasFor} 把 {@link #basePackages} 真转发给
 * {@code FeignClientsRegistrar}，从而扫描指定包下的 Feign 客户端接口。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableFeignClients
public @interface EnableSdkClients {

    /**
     * 扫描包路径，转发给 {@link EnableFeignClients#basePackages()}
     */
    @AliasFor(annotation = EnableFeignClients.class, attribute = "basePackages")
    String[] basePackages() default {};
}
