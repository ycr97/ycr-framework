package com.ycr.framework.security.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 安全模块自动配置测试
 *
 * <p>验证 Servlet Web 环境下默认注册鉴权拦截器配置，且 {@code ycr.security.enabled=false}
 * 时不注册；非 Web 环境亦不注册。</p>
 */
class SecurityAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class));

    @Test
    void 默认只应注册权限校验器() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean("authorizeAspect");
            assertThat(context).hasBean("permissionChecker");
        });
    }

    @Test
    void 显式开启时应注册鉴权切面() {
        runner.withPropertyValues("ycr.security.enabled=true")
                .run(context -> {
                    assertThat(context).hasBean("authorizeAspect");
                    assertThat(context).hasBean("permissionChecker");
                });
    }
}
