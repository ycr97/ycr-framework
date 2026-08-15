package com.ycr.framework.business.autoconfigure;

import com.ycr.framework.business.aop.BizApiAspect;
import com.ycr.framework.business.chain.BizContext;
import com.ycr.framework.business.chain.BizInterceptor;
import com.ycr.framework.business.chain.BizInterceptorChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 接入层拦截链自动配置测试
 *
 * @author ycr
 */
class BusinessAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BusinessAutoConfiguration.class));

    @Test
    @DisplayName("默认装配链与切面")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(BizInterceptorChain.class);
            assertThat(context).hasSingleBean(BizApiAspect.class);
        });
    }

    @Test
    @DisplayName("容器内拦截器被纳入链")
    void shouldMatchExpectedBehavior002() {
        runner.withUserConfiguration(InterceptorConfig.class).run(context -> {
            List<String> trace = context.getBean("trace", List.class);
            BizInterceptorChain chain = context.getBean(BizInterceptorChain.class);
            chain.execute(new BizContext(null, null, new Object[0], null), () -> "r");
            assertThat(trace).containsExactly("before");
        });
    }

    @Test
    @DisplayName("关闭开关时不装配")
    void shouldMatchExpectedBehavior003() {
        runner.withPropertyValues("ycr.business.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(BizInterceptorChain.class);
            assertThat(context).doesNotHaveBean(BizApiAspect.class);
        });
    }

    @Configuration
    static class InterceptorConfig {
        @Bean
        List<String> trace() {
            return new ArrayList<>();
        }

        @Bean
        BizInterceptor recorder(List<String> trace) {
            return new BizInterceptor() {
                @Override
                public void before(BizContext c) {
                    trace.add("before");
                }
            };
        }
    }
}
