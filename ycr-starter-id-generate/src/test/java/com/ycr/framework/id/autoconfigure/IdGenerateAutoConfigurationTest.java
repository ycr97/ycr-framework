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
    @DisplayName("应装配默认生成器并允许用户覆盖")
    void shouldMatchExpectedBehavior001() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(IdGenerateAutoConfiguration.class));
        runner.run(context -> assertThat(context.getBean(IdGenerator.class))
                .isInstanceOf(SnowflakeIdGenerator.class));

        IdGenerator custom = () -> 1L;
        runner.withBean(IdGenerator.class, () -> custom)
                .run(context -> assertThat(context.getBean(IdGenerator.class)).isSameAs(custom));
    }
}
