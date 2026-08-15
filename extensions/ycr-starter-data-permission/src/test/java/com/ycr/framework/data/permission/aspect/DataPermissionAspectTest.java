package com.ycr.framework.data.permission.aspect;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.ycr.framework.data.permission.annotation.DataPermission;
import com.ycr.framework.data.permission.annotation.DataPermissionIgnore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据权限注解切面测试
 *
 * <p>用例在被代理的 Bean 方法内部读取 {@link InterceptorIgnoreHelper#willIgnoreDataPermission(String)}，
 * 验证切面是否在方法执行期间正确地压入/未压入忽略策略，并在调用结束后完成清理。</p>
 */
class DataPermissionAspectTest {

    private static final String ANY_MS_ID = "com.demo.AnyMapper.select";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class))
            .withBean(DataPermissionAspect.class)
            .withBean(DemoService.class)
            .withBean(IgnoredClassService.class);

    @Test
    @DisplayName("方法级Ignore注解应忽略数据权限")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> {
            DemoService service = context.getBean(DemoService.class);
            assertTrue(service.ignoredMethod(), "标注 @DataPermissionIgnore 的方法内应忽略数据权限");
            // 调用结束后线程级策略应被清理
            assertFalse(InterceptorIgnoreHelper.hasIgnoreStrategy(), "调用结束后应清理忽略策略");
        });
    }

    @Test
    @DisplayName("普通方法不应忽略数据权限")
    void shouldMatchExpectedBehavior002() {
        runner.run(context -> {
            DemoService service = context.getBean(DemoService.class);
            assertFalse(service.plainMethod(), "无注解方法应维持默认（数据权限生效）");
        });
    }

    @Test
    @DisplayName("类级Ignore下方法级DataPermission应强制启用")
    void shouldMatchExpectedBehavior003() {
        runner.run(context -> {
            IgnoredClassService service = context.getBean(IgnoredClassService.class);
            assertTrue(service.normalQuery(), "类级 @DataPermissionIgnore 应忽略数据权限");
            assertFalse(service.reEnabledQuery(), "方法级 @DataPermission 应覆盖类级忽略，强制启用");
        });
    }

    /** 方法级注解示例 */
    static class DemoService {

        @DataPermissionIgnore
        public boolean ignoredMethod() {
            return InterceptorIgnoreHelper.willIgnoreDataPermission(ANY_MS_ID);
        }

        public boolean plainMethod() {
            return InterceptorIgnoreHelper.willIgnoreDataPermission(ANY_MS_ID);
        }
    }

    /** 类级忽略 + 方法级重新启用示例 */
    @DataPermissionIgnore
    static class IgnoredClassService {

        public boolean normalQuery() {
            return InterceptorIgnoreHelper.willIgnoreDataPermission(ANY_MS_ID);
        }

        @DataPermission
        public boolean reEnabledQuery() {
            return InterceptorIgnoreHelper.willIgnoreDataPermission(ANY_MS_ID);
        }
    }
}
