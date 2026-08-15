package com.ycr.framework.ddd.extension;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 扩展实现注解
 *
 * <p>标注扩展点实现类 + 兼作 Spring {@link Component}（自动注册为 Bean，由 {@code ExtensionBootstrap}
 * 按 bizId/useCase/scenario 注册到 {@code ExtensionRepository}）。</p>
 *
 * @author ycr
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Extension {

    /** 业务身份 */
    String bizId() default BizScenario.DEFAULT_BIZ_ID;

    /** 用例 */
    String useCase() default BizScenario.DEFAULT_USE_CASE;

    /** 场景 */
    String scenario() default BizScenario.DEFAULT_SCENARIO;
}
