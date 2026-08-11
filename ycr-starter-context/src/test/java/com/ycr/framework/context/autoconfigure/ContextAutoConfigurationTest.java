package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.sign.ContextHeaderSigner;
import com.ycr.framework.context.propagation.ContextTaskDecorator;
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
    @DisplayName("应装配与运行环境无关的签名能力")
    void shouldMatchExpectedBehavior001() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ContextAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(ContextHeaderSigner.class);
                });
    }

    @Test
    @DisplayName("缺少ServletApi时仍应保留通用签名能力")
    void shouldMatchExpectedBehavior002() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("jakarta.servlet"))
                .withConfiguration(AutoConfigurations.of(
                        ContextAutoConfiguration.class,
                        ContextTaskExecutionAutoConfiguration.class,
                        ContextServletAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ContextHeaderSigner.class);
                    assertThat(context).hasSingleBean(ContextTaskDecorator.class);
                    assertThat(context).doesNotHaveBean("contextFilterRegistration");
                });
    }
}
