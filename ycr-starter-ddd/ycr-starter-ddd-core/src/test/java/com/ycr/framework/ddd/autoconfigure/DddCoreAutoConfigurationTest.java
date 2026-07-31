package com.ycr.framework.ddd.autoconfigure;

import com.ycr.framework.ddd.event.DomainEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DddCoreAutoConfiguration 测试
 *
 * @author ycr
 */
class DddCoreAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DddCoreAutoConfiguration.class));

    @Test
    @DisplayName("装配DomainEventPublisher")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> assertThat(context).hasSingleBean(DomainEventPublisher.class));
    }

    @Test
    @DisplayName("已有自定义时让位")
    void shouldMatchExpectedBehavior002() {
        runner.withBean(DomainEventPublisher.class,
                        () -> new DomainEventPublisher(org.mockito.Mockito.mock(ApplicationEventPublisher.class)))
                .run(context -> assertThat(context).hasSingleBean(DomainEventPublisher.class));
    }
}
