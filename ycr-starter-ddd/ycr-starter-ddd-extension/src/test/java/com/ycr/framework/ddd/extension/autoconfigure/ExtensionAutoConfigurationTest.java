package com.ycr.framework.ddd.extension.autoconfigure;

import com.ycr.framework.ddd.extension.ExtensionBootstrap;
import com.ycr.framework.ddd.extension.ExtensionExecutor;
import com.ycr.framework.ddd.extension.ExtensionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExtensionAutoConfiguration 测试
 *
 * @author ycr
 */
class ExtensionAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExtensionAutoConfiguration.class));

    @Test
    void 装配仓库_执行器_引导器() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(ExtensionRepository.class);
            assertThat(context).hasSingleBean(ExtensionExecutor.class);
            assertThat(context).hasSingleBean(ExtensionBootstrap.class);
        });
    }
}
