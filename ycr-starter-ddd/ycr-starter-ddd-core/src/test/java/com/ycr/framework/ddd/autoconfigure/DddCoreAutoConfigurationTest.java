package com.ycr.framework.ddd.autoconfigure;

import com.ycr.framework.ddd.event.DomainEventPublisher;
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
    void 装配DomainEventPublisher() {
        runner.run(context -> assertThat(context).hasSingleBean(DomainEventPublisher.class));
    }

    @Test
    void 已有自定义时让位() {
        runner.withBean(DomainEventPublisher.class,
                        () -> new DomainEventPublisher(org.mockito.Mockito.mock(ApplicationEventPublisher.class)))
                .run(context -> assertThat(context).hasSingleBean(DomainEventPublisher.class));
    }
}
