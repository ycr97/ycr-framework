package com.ycr.framework.captcha.autoconfigure;

import com.ycr.framework.captcha.model.CaptchaResult;
import com.ycr.framework.captcha.service.CaptchaService;
import com.ycr.framework.captcha.service.HutoolCaptchaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CaptchaAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class CaptchaAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CaptchaAutoConfiguration.class));

    @Test
    @DisplayName("默认应装配Hutool实现")
    void shouldMatchExpectedBehavior001() {
        runner.run(context -> {
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
