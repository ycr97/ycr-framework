package com.ycr.framework.tenant.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.ycr.framework.tenant.handler.YcrTenantLineHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TenantAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class TenantAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TenantAutoConfiguration.class));

    @Test
    void 开启时应装配处理器与租户拦截器() {
        runner.withPropertyValues("ycr.tenant.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(YcrTenantLineHandler.class);
            assertThat(context).hasSingleBean(InnerInterceptor.class);
            assertThat(context.getBean(InnerInterceptor.class)).isInstanceOf(TenantLineInnerInterceptor.class);
        });
    }

    @Test
    void 默认关闭时不装配() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(YcrTenantLineHandler.class);
            assertThat(context).doesNotHaveBean(InnerInterceptor.class);
        });
    }
}
