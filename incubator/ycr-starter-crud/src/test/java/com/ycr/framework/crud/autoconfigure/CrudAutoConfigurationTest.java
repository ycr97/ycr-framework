package com.ycr.framework.crud.autoconfigure;

import com.ycr.framework.crud.mapping.CrudApiRequestMappingHandlerMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrudAutoConfiguration 测试
 *
 * @author ycr
 */
class CrudAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CrudAutoConfiguration.class));

    @Test
    @DisplayName("默认关闭时不应装配MVC扩展")
    void disabledByDefaultShouldNotConfigureMvcRegistrations() {
        runner.run(context -> assertThat(context).doesNotHaveBean(WebMvcRegistrations.class));
    }

    @Test
    @DisplayName("装配WebMvcRegistrations并回传自定义HandlerMapping")
    void shouldMatchExpectedBehavior001() {
        runner.withPropertyValues("ycr.crud.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(WebMvcRegistrations.class);
            WebMvcRegistrations registrations = context.getBean(WebMvcRegistrations.class);
            assertThat(registrations.getRequestMappingHandlerMapping())
                    .isInstanceOf(CrudApiRequestMappingHandlerMapping.class);
        });
    }

    @Test
    @DisplayName("启用CRUD但应用自定义WebMvcRegistrations不兼容时应启动失败")
    void incompatibleCustomWebMvcRegistrationsShouldFailFast() {
        runner.withBean(WebMvcRegistrations.class, () -> new WebMvcRegistrations() {
        }).withPropertyValues("ycr.crud.enabled=true")
                .run(context -> assertThat(context.getStartupFailure()).hasMessage(
                        "ycr.crud.enabled=true requires the single WebMvcRegistrations bean "
                                + "to return CrudApiRequestMappingHandlerMapping"));
    }

    @Test
    @DisplayName("应用自定义WebMvcRegistrations显式返回YCR映射时应允许启动")
    void compatibleCustomWebMvcRegistrationsShouldBeAccepted() {
        runner.withBean(WebMvcRegistrations.class, () -> new WebMvcRegistrations() {
            @Override
            public org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
                    getRequestMappingHandlerMapping() {
                return new CrudApiRequestMappingHandlerMapping();
            }
        }).withPropertyValues("ycr.crud.enabled=true")
                .run(context -> assertThat(context).hasNotFailed());
    }
}
