package com.ycr.framework.tenant.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.ycr.framework.data.mp.autoconfigure.MybatisPlusAutoConfiguration;
import com.ycr.framework.tenant.handler.YcrTenantLineHandler;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("开启时应装配处理器与租户拦截器")
    void shouldMatchExpectedBehavior001() {
        runner.withPropertyValues("ycr.tenant.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(YcrTenantLineHandler.class);
            assertThat(context).hasSingleBean(InnerInterceptor.class);
            assertThat(context.getBean(InnerInterceptor.class)).isInstanceOf(TenantLineInnerInterceptor.class);
        });
    }

    @Test
    @DisplayName("默认关闭时不装配")
    void shouldMatchExpectedBehavior002() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(YcrTenantLineHandler.class);
            assertThat(context).doesNotHaveBean(InnerInterceptor.class);
        });
    }

    @Test
    @DisplayName("用户自定义MyBatisPlus拦截器时仍应在分页前织入租户隔离")
    void shouldMergeTenantInterceptorBeforePagination() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        MybatisPlusAutoConfiguration.class, TenantAutoConfiguration.class))
                .withBean("customMybatisPlusInterceptor", MybatisPlusInterceptor.class, () -> {
                    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
                    interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
                    return interceptor;
                })
                .withPropertyValues("ycr.tenant.enabled=true")
                .run(context -> {
                    MybatisPlusInterceptor interceptor = context.getBean(MybatisPlusInterceptor.class);
                    assertThat(interceptor.getInterceptors()).hasSize(2);
                    assertThat(interceptor.getInterceptors().get(0)).isInstanceOf(TenantLineInnerInterceptor.class);
                    assertThat(interceptor.getInterceptors().get(1)).isInstanceOf(PaginationInnerInterceptor.class);
                });
    }
}
