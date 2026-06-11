package com.ycr.framework.log.autoconfigure;

import com.ycr.framework.log.aop.LogAspect;
import com.ycr.framework.log.handler.LogHandler;
import com.ycr.framework.log.handler.Slf4jLogHandler;
import com.ycr.framework.log.model.LogRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LogAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class LogAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LogAutoConfiguration.class));

    @Test
    void 默认应装配切面与默认处理器() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(LogAspect.class);
            assertThat(context).hasSingleBean(LogHandler.class);
            assertThat(context.getBean(LogHandler.class)).isInstanceOf(Slf4jLogHandler.class);
        });
    }

    @Test
    void 关闭开关时不装配切面() {
        runner.withPropertyValues("ycr.log.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(LogAspect.class));
    }

    @Test
    void 业务自定义处理器应覆盖默认() {
        runner.withUserConfiguration(CustomHandlerConfig.class).run(context -> {
            assertThat(context).hasSingleBean(LogHandler.class);
            assertThat(context.getBean(LogHandler.class)).isInstanceOf(CustomLogHandler.class);
        });
    }

    @Test
    void 异步开启时装配执行器() {
        runner.withPropertyValues("ycr.log.async=true")
                .run(context -> assertThat(context.containsBean("ycrLogExecutor")).isTrue());
    }

    @Test
    void 默认不装配异步执行器() {
        runner.run(context -> assertThat(context.containsBean("ycrLogExecutor")).isFalse());
    }

    @Configuration
    static class CustomHandlerConfig {
        @Bean
        LogHandler customLogHandler() {
            return new CustomLogHandler();
        }
    }

    static class CustomLogHandler implements LogHandler {
        @Override
        public void handle(LogRecord logRecord) {
            // 仅用于覆盖测试
        }
    }
}
