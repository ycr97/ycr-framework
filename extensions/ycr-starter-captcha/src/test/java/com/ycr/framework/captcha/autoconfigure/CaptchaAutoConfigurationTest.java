package com.ycr.framework.captcha.autoconfigure;

import com.ycr.framework.captcha.model.CaptchaResult;
import com.ycr.framework.captcha.service.CaptchaService;
import com.ycr.framework.captcha.service.HutoolCaptchaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * CaptchaAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class CaptchaAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CaptchaAutoConfiguration.class));

    @Test
    @DisplayName("默认应关闭验证码")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> assertThat(context).doesNotHaveBean(CaptchaService.class));
    }

    @Test
    @DisplayName("显式开启且存在Redisson时应装配Hutool实现")
    void shouldConfigureCaptchaServiceWhenEnabledWithRedisson() {
        runner.withBean(RedissonClient.class, () -> mock(RedissonClient.class))
                .withPropertyValues("ycr.captcha.enabled=true")
                .run(context -> {
            assertThat(context).hasSingleBean(CaptchaService.class);
            assertThat(context.getBean(CaptchaService.class)).isInstanceOf(HutoolCaptchaService.class);
        });
    }

    @Test
    @DisplayName("关闭开关时不装配")
    void shouldMatchExpectedBehavior002() {
        runner.withPropertyValues("ycr.captcha.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(CaptchaService.class));
    }

    @Test
    @DisplayName("业务自定义实现应覆盖默认")
    void shouldMatchExpectedBehavior003() {
        runner.withUserConfiguration(CustomConfig.class).run(context -> {
            assertThat(context).hasSingleBean(CaptchaService.class);
            assertThat(context.getBean(CaptchaService.class)).isInstanceOf(CustomCaptchaService.class);
        });
    }

    @Test
    @DisplayName("显式开启但无Redisson时应启动失败")
    void shouldFailWhenEnabledWithoutRedissonClient() {
        runner.withPropertyValues("ycr.captcha.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "ycr.captcha.enabled=true requires a RedissonClient; "
                                    + "configure ycr-starter-cache and Redis");
                });
    }

    @Configuration
    static class CustomConfig {
        @Bean
        CaptchaService customCaptchaService() {
            return new CustomCaptchaService();
        }
    }

    static class CustomCaptchaService implements CaptchaService {
        @Override
        public CaptchaResult generate() {
            return null;
        }

        @Override
        public boolean verify(String id, String code) {
            return false;
        }
    }
}
