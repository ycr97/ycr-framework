package com.ycr.framework.id.autoconfigure;

import com.ycr.framework.id.generator.IdGenerator;
import com.ycr.framework.id.generator.SnowflakeIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ID 生成自动配置测试。
 *
 * @author ycr
 */
class IdGenerateAutoConfigurationTest {

    @Test
    @DisplayName("默认不应启用固定节点的雪花算法")
    void shouldRemainDisabledByDefault() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(IdGenerateAutoConfiguration.class));

        runner.run(context -> assertThat(context).doesNotHaveBean(IdGenerator.class));
    }

    @Test
    @DisplayName("启用雪花算法时必须配置节点号")
    void shouldFailWhenEnabledWithoutNodeIds() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(IdGenerateAutoConfiguration.class))
                .withPropertyValues("ycr.id.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("ycr.id.worker-id 和 ycr.id.datacenter-id 必须在启用雪花 ID 时显式配置");
                });
    }

    @Test
    @DisplayName("配置唯一节点号后应装配雪花生成器")
    void shouldCreateSnowflakeGeneratorWhenNodeIdsAreConfigured() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(IdGenerateAutoConfiguration.class))
                .withPropertyValues(
                        "ycr.id.enabled=true",
                        "ycr.id.worker-id=2",
                        "ycr.id.datacenter-id=3")
                .run(context -> assertThat(context.getBean(IdGenerator.class))
                        .isInstanceOf(SnowflakeIdGenerator.class));
    }

    @Test
    @DisplayName("用户自定义生成器应覆盖默认实现")
    void shouldBackOffWhenCustomGeneratorExists() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(IdGenerateAutoConfiguration.class));

        IdGenerator custom = () -> 1L;
        runner.withBean(IdGenerator.class, () -> custom)
                .run(context -> assertThat(context.getBean(IdGenerator.class)).isSameAs(custom));
    }
}
