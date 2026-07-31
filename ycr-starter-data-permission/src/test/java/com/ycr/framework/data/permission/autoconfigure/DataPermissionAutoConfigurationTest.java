package com.ycr.framework.data.permission.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.ycr.framework.data.permission.aspect.DataPermissionAspect;
import com.ycr.framework.data.permission.scope.DataScopeClearFilter;
import com.ycr.framework.data.permission.scope.DataScopeResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataPermissionAutoConfiguration 装配与开关测试
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
    void 显式开启时应装配新版权限链路() {
        runner.withPropertyValues("ycr.data.permission.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(InnerInterceptor.class);
            assertThat(context).hasSingleBean(DataPermissionAspect.class);
            assertThat(context).hasSingleBean(MultiDataPermissionHandler.class);
            assertThat(context).hasSingleBean(DataScopeResolver.class);   // 缺省空 → fail-closed
            assertThat(context).hasSingleBean(DataScopeClearFilter.class);
        });
    }

    @Test
    void 显式开启但无servletApi时不装配清理filter() {
        runner.withPropertyValues("ycr.data.permission.enabled=true")
                .withClassLoader(new FilteredClassLoader(jakarta.servlet.http.HttpServletRequest.class))
                .run(context -> assertThat(context).doesNotHaveBean(DataScopeClearFilter.class));
    }
}
