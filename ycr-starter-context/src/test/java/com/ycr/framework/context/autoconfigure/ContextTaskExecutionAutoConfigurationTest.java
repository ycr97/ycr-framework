package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.propagation.ContextTaskDecorator;
import com.ycr.framework.context.propagation.ThreadContextAccessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.TaskDecorator;

import static org.assertj.core.api.Assertions.assertThat;

class ContextTaskExecutionAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ContextTaskExecutionAutoConfiguration.class));

    @Test
    @DisplayName("应默认装配统一上下文任务装饰器")
    void shouldConfigureContextTaskDecoratorByDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(ThreadContextAccessor.class);
            assertThat(context).hasSingleBean(ContextTaskDecorator.class);
        });
    }

    @Test
    @DisplayName("用户自定义任务装饰器时应让位")
    void shouldBackOffForCustomTaskDecorator() {
        TaskDecorator custom = runnable -> runnable;
        runner.withBean(TaskDecorator.class, () -> custom)
                .run(context -> assertThat(context.getBean(TaskDecorator.class)).isSameAs(custom));
    }
}
