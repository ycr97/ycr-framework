package com.ycr.framework.data.permission.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
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
    void 默认装配核心组件与缺省空resolver() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(MultiDataPermissionHandler.class);
            assertThat(context).hasSingleBean(DataScopeResolver.class);   // 缺省空 → fail-closed
            assertThat(context).hasSingleBean(DataScopeClearFilter.class);
        });
    }

    @Test
    void 关闭开关时不装配拦截器() {
        runner.withPropertyValues("ycr.data.permission.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean("dataPermissionInnerInterceptor"));
    }

    @Test
    void 无servletApi时不装配清理filter() {
        runner.withClassLoader(new FilteredClassLoader(jakarta.servlet.http.HttpServletRequest.class))
                .run(context -> assertThat(context).doesNotHaveBean(DataScopeClearFilter.class));
    }
}
