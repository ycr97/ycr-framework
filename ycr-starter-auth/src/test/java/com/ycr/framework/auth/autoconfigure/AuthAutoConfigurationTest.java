package com.ycr.framework.auth.autoconfigure;

import com.ycr.framework.auth.handler.SaTokenExceptionHandler;
import com.ycr.framework.auth.resolver.SaTokenUserContextResolver;
import com.ycr.framework.context.resolver.UserContextResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 认证模块自动配置测试。
 *
 * @author ycr
 */
class AuthAutoConfigurationTest {

    @Test
    void 应装配异常处理器和SaToken上下文解析器() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AuthAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(SaTokenExceptionHandler.class);
                    assertThat(context).hasSingleBean(UserContextResolver.class);
                    assertThat(context.getBean(UserContextResolver.class))
                            .isInstanceOf(SaTokenUserContextResolver.class);
                });
    }
}
