package com.ycr.framework.crud.autoconfigure;

import com.ycr.framework.crud.mapping.CrudApiRequestMappingHandlerMapping;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrudAutoConfiguration 测试
 *
 * @author ycr
 */
class CrudAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CrudAutoConfiguration.class));

    @Test
    void 装配WebMvcRegistrations并回传自定义HandlerMapping() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(WebMvcRegistrations.class);
            WebMvcRegistrations registrations = context.getBean(WebMvcRegistrations.class);
            assertThat(registrations.getRequestMappingHandlerMapping())
                    .isInstanceOf(CrudApiRequestMappingHandlerMapping.class);
        });
    }

    @Test
    void 应用已自定义WebMvcRegistrations时让位() {
        runner.withBean(WebMvcRegistrations.class, () -> new WebMvcRegistrations() {
        }).run(context -> assertThat(context).hasSingleBean(WebMvcRegistrations.class));
    }
}
