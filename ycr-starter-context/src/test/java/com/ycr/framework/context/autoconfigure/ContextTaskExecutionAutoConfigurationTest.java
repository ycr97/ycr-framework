package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.propagation.ContextTaskDecorator;
import com.ycr.framework.context.propagation.ThreadContextAccessor;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.TaskDecorator;

import java.util.concurrent.atomic.AtomicBoolean;

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
    @DisplayName("用户自定义任务装饰器时应与上下文传播组合")
    void customTaskDecoratorShouldComposeWithContextPropagation() {
        AtomicBoolean customDecoratorExecuted = new AtomicBoolean();
        TaskDecorator custom = runnable -> () -> {
            customDecoratorExecuted.set(true);
            runnable.run();
        };
        runner.withBean(TaskDecorator.class, () -> custom)
                .run(context -> {
                    assertThat(context).getBeans(TaskDecorator.class).hasSize(2);
                    assertThat(context.getBean(TaskDecorator.class)).isInstanceOf(ContextTaskDecorator.class);

                    UserContext user = new UserContext();
                    user.setUserId(1001L);
                    UserContextHolder.set(user);
                    AtomicBoolean contextVisible = new AtomicBoolean();
                    Runnable task = context.getBean(TaskDecorator.class)
                            .decorate(() -> contextVisible.set(UserContextHolder.getUserId() == 1001L));
                    UserContextHolder.clear();
                    task.run();

                    assertThat(customDecoratorExecuted).isTrue();
                    assertThat(contextVisible).isTrue();
                    assertThat(UserContextHolder.get()).isNull();
                });
    }
}
