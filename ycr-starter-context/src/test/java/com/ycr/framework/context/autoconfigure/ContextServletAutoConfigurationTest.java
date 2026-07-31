package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.filter.ContextFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Servlet 上下文过滤器自动配置测试。
 *
 * @author ycr
 */
class ContextServletAutoConfigurationTest {

    @Test
    @DisplayName("Servlet环境应装配上下文过滤器")
    void shouldMatchExpectedBehavior001() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ContextAutoConfiguration.class,
                        ContextServletAutoConfiguration.class))
                .run(context -> {
                    FilterRegistrationBean<?> registration = context.getBean(FilterRegistrationBean.class);
                    assertThat(registration.getFilter()).isInstanceOf(ContextFilter.class);
                });
    }
}
