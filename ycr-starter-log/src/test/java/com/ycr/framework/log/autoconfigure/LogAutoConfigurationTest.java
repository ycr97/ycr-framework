package com.ycr.framework.log.autoconfigure;

import com.ycr.framework.log.aop.LogAspect;
import com.ycr.framework.log.handler.LogHandler;
import com.ycr.framework.log.handler.Slf4jLogHandler;
import com.ycr.framework.log.model.LogRecord;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("默认应装配切面与默认处理器")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(LogAspect.class);
            assertThat(context).hasSingleBean(LogHandler.class);
            assertThat(context.getBean(LogHandler.class)).isInstanceOf(Slf4jLogHandler.class);
        });
    }

    @Test
    @DisplayName("关闭开关时不装配切面")
    void shouldMatchExpectedBehavior002() {
        runner.withPropertyValues("ycr.log.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(LogAspect.class));
    }

    @Test
    @DisplayName("业务自定义处理器应覆盖默认")
    void shouldMatchExpectedBehavior003() {
        runner.withUserConfiguration(CustomHandlerConfig.class).run(context -> {
            assertThat(context).hasSingleBean(LogHandler.class);
            assertThat(context.getBean(LogHandler.class)).isInstanceOf(CustomLogHandler.class);
        });
    }

    @Test
    @DisplayName("异步开启时装配执行器")
    void shouldMatchExpectedBehavior004() {
        runner.withPropertyValues("ycr.log.async=true")
                .run(context -> assertThat(context.containsBean("ycrLogExecutor")).isTrue());
    }

    @Test
    @DisplayName("默认不装配异步执行器")
    void shouldMatchExpectedBehavior005() {
        runner.run(context -> assertThat(context.containsBean("ycrLogExecutor")).isFalse());
    }

    @Test
    @DisplayName("应装配序列化管线与默认归属地解析器")
    void shouldMatchExpectedBehavior006() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(com.ycr.framework.log.util.LogJsonSupport.class);
            assertThat(context).hasSingleBean(com.ycr.framework.log.handler.IpRegionResolver.class);
        });
    }

    @Test
    @DisplayName("业务自定义归属地解析器应覆盖默认")
    void shouldMatchExpectedBehavior007() {
        runner.withBean(com.ycr.framework.log.handler.IpRegionResolver.class, () -> ip -> "X")
                .run(context -> assertThat(
                        context.getBean(com.ycr.framework.log.handler.IpRegionResolver.class).resolve("1"))
                        .isEqualTo("X"));
    }

    @Test
    @DisplayName("默认应装配方法日志切面")
    void shouldMatchExpectedBehavior008() {
        runner.run(context ->
                assertThat(context).hasSingleBean(com.ycr.framework.log.aspect.MethodLogAspect.class));
    }

    @Test
    @DisplayName("关闭开关时不装配方法日志切面")
    void shouldMatchExpectedBehavior009() {
        runner.withPropertyValues("ycr.log.method.enabled=false")
                .run(context ->
                        assertThat(context).doesNotHaveBean(com.ycr.framework.log.aspect.MethodLogAspect.class));
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
