package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.resolver.UserContextResolverChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.FilteredClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 上下文基础自动配置测试。
 *
 * @author ycr
 */
class ContextAutoConfigurationTest {

    @Test
    @DisplayName("有ServletApi时应装配解析链")
    void shouldMatchExpectedBehavior001() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ContextAutoConfiguration.class))
                .run(context -> assertThat(context).hasSingleBean(UserContextResolverChain.class));
    }

    @Test
    @DisplayName("缺少ServletApi时应跳过自动配置且正常启动")
    void shouldMatchExpectedBehavior002() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("jakarta.servlet"))
                .withConfiguration(AutoConfigurations.of(ContextAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(UserContextResolverChain.class);
                });
    }
}
