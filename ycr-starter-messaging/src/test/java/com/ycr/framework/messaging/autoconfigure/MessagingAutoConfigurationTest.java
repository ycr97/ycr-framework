package com.ycr.framework.messaging.autoconfigure;

import com.ycr.framework.messaging.mail.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * MessagingAutoConfiguration 装配与开关测试
 *
 * @author ycr
 */
class MessagingAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MessagingAutoConfiguration.class));

    @Test
    void 存在JavaMailSender时应装配MailService() {
        runner.withUserConfiguration(MailConfig.class)
                .run(context -> assertThat(context).hasSingleBean(MailService.class));
    }

    @Test
    void 关闭开关时不装配() {
        runner.withUserConfiguration(MailConfig.class)
                .withPropertyValues("ycr.messaging.mail-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(MailService.class));
    }

    @Test
    void 无JavaMailSender时不装配() {
        runner.run(context -> assertThat(context).doesNotHaveBean(MailService.class));
    }

    @Configuration
    static class MailConfig {
        @Bean
        JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }

        @Bean
        MailProperties mailProperties() {
            return new MailProperties();
        }
    }
}
