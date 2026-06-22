package com.ycr.framework.data.permission.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.ycr.framework.data.permission.aspect.DataPermissionAspect;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据权限自动装配测试。
 *
 * @author ycr
 */
class DataPermissionAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DataPermissionAutoConfiguration.class));

    @Test
    void 默认不应装配SQL拦截器与切面() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(InnerInterceptor.class);
            assertThat(context).doesNotHaveBean(DataPermissionAspect.class);
        });
    }

    @Test
    void 显式开启时应装配SQL拦截器与切面() {
        runner.withPropertyValues("ycr.data.permission.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(InnerInterceptor.class);
            assertThat(context).hasSingleBean(DataPermissionAspect.class);
        });
    }
}
