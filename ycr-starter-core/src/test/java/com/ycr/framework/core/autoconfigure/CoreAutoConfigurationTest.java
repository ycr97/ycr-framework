package com.ycr.framework.core.autoconfigure;

import com.ycr.framework.core.util.SpringContextHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 核心模块自动配置测试。
 *
 * @author ycr
 */
class CoreAutoConfigurationTest {

    @Test
    @DisplayName("应装配SpringContextHolder")
    void shouldMatchExpectedBehavior001() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CoreAutoConfiguration.class))
                .run(context -> assertThat(context).hasSingleBean(SpringContextHolder.class));
    }
}
