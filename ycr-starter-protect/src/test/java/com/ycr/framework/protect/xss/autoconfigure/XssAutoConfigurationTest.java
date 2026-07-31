package com.ycr.framework.protect.xss.autoconfigure;

import com.ycr.framework.protect.xss.filter.XssFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * XSS 自动装配测试。
 *
 * @author ycr
 */
class XssAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(XssAutoConfiguration.class));

    @Test
    void 默认不应注册XssFilter() {
        runner.run(context -> assertThat(context).doesNotHaveBean(FilterRegistrationBean.class));
    }

    @Test
    void 显式开启时应注册XssFilter() {
        runner.withPropertyValues("ycr.protect.xss.enabled=true").run(context -> {
            FilterRegistrationBean<?> registration = context.getBean(FilterRegistrationBean.class);
            assertThat(registration.getFilter()).isInstanceOf(XssFilter.class);
        });
    }
}
