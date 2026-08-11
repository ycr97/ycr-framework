package com.ycr.framework.data.permission.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.ycr.framework.data.mp.autoconfigure.MybatisPlusAutoConfiguration;
import com.ycr.framework.data.permission.aspect.DataPermissionAspect;
import com.ycr.framework.data.permission.rule.DataPermissionRule;
import com.ycr.framework.data.permission.rule.Predicate;
import com.ycr.framework.data.permission.scope.DataScope;
import com.ycr.framework.data.permission.scope.DataScopeClearFilter;
import com.ycr.framework.data.permission.scope.DataScopeResolver;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("默认不应装配SQL拦截器与切面")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(InnerInterceptor.class);
            assertThat(context).doesNotHaveBean(DataPermissionAspect.class);
        });
    }

    @Test
    @DisplayName("显式开启时应装配新版权限链路")
    void shouldMatchExpectedBehavior002() {
        enabledRunner().run(context -> {
            assertThat(context).hasSingleBean(InnerInterceptor.class);
            assertThat(context).hasSingleBean(DataPermissionAspect.class);
            assertThat(context).hasSingleBean(MultiDataPermissionHandler.class);
            assertThat(context).hasSingleBean(DataScopeResolver.class);   // 缺省空 → fail-closed
            assertThat(context).hasSingleBean(DataScopeClearFilter.class);
        });
    }

    @Test
    @DisplayName("显式开启但无servletApi时不装配清理filter")
    void shouldMatchExpectedBehavior003() {
        enabledRunner()
                .withClassLoader(new FilteredClassLoader(jakarta.servlet.http.HttpServletRequest.class))
                .run(context -> assertThat(context).doesNotHaveBean(DataScopeClearFilter.class));
    }

    @Test
    @DisplayName("启用数据权限但未声明受治理表时应启动失败")
    void shouldFailWhenGovernedTablesAreMissing() {
        runner.withBean(DataPermissionRule.class, this::orderRule)
                .withPropertyValues("ycr.data.permission.enabled=true")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseMessage("ycr.data.permission.governed-tables 必须在启用数据权限时显式配置"));
    }

    @Test
    @DisplayName("受治理表没有对应规则时应启动失败")
    void shouldFailWhenGovernedTableHasNoRule() {
        runner.withPropertyValues(
                        "ycr.data.permission.enabled=true",
                        "ycr.data.permission.governed-tables=orders")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseMessage("受治理表缺少 DataPermissionRule: [orders]"));
    }

    @Test
    @DisplayName("用户自定义MyBatisPlus拦截器时仍应织入数据权限")
    void shouldMergeDataPermissionIntoCustomMybatisPlusInterceptor() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        MybatisPlusAutoConfiguration.class, DataPermissionAutoConfiguration.class))
                .withBean("customMybatisPlusInterceptor", MybatisPlusInterceptor.class,
                        MybatisPlusInterceptor::new)
                .withBean(DataPermissionRule.class, this::orderRule)
                .withPropertyValues(
                        "ycr.data.permission.enabled=true",
                        "ycr.data.permission.governed-tables=orders")
                .run(context -> assertThat(context.getBean(MybatisPlusInterceptor.class).getInterceptors())
                        .anyMatch(DataPermissionInterceptor.class::isInstance));
    }

    private ApplicationContextRunner enabledRunner() {
        return runner.withBean(DataPermissionRule.class, this::orderRule)
                .withPropertyValues(
                        "ycr.data.permission.enabled=true",
                        "ycr.data.permission.governed-tables=orders");
    }

    private DataPermissionRule orderRule() {
        return new DataPermissionRule() {
            @Override
            public String table() {
                return "orders";
            }

            @Override
            public Predicate predicate(DataScope scope) {
                return Predicate.deny();
            }
        };
    }
}
