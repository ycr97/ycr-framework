package com.ycr.framework.apidoc.autoconfigure;

import com.ycr.framework.apidoc.filter.ApiDocDisabledFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

class ApiDocDisabledAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ApiDocDisabledAutoConfiguration.class));

    @Test
    @DisplayName("文档关闭时应装配最高优先级阻断Filter")
    void disabledDocumentationShouldRegisterBlockingFilter() {
        runner.withPropertyValues("ycr.api-doc.enabled=false")
                .run(context -> {
                    FilterRegistrationBean<?> registration = context.getBean(FilterRegistrationBean.class);
                    assertThat(registration.getFilter()).isInstanceOf(ApiDocDisabledFilter.class);
                    assertThat(registration.getOrder()).isEqualTo(Integer.MIN_VALUE);
                });
    }

    @Test
    @DisplayName("文档开启时不应装配阻断Filter")
    void enabledDocumentationShouldNotRegisterBlockingFilter() {
        runner.withPropertyValues("ycr.api-doc.enabled=true")
                .run(context -> assertThat(context).doesNotHaveBean(FilterRegistrationBean.class));
    }
}
