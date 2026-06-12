package com.ycr.framework.messaging.autoconfigure;

import com.ycr.framework.messaging.mail.DefaultMailService;
import com.ycr.framework.messaging.mail.MailService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 消息通知自动配置
 *
 * <p>在 Spring Boot 已创建 {@link JavaMailSender}（即配置了 {@code spring.mail.host}）时装配
 * {@link MailService}。通过 {@code ycr.messaging.mail-enabled=false} 关闭。</p>
 *
 * @author ycr
 */
@AutoConfiguration(after = MailSenderAutoConfiguration.class)
@ConditionalOnClass(JavaMailSender.class)
@EnableConfigurationProperties(MessagingProperties.class)
@ConditionalOnProperty(prefix = "ycr.messaging", name = "mail-enabled", havingValue = "true", matchIfMissing = true)
public class MessagingAutoConfiguration {

    @Bean
    @ConditionalOnBean(JavaMailSender.class)
    @ConditionalOnMissingBean
    public MailService mailService(JavaMailSender mailSender, MessagingProperties properties,
                                   MailProperties bootMailProperties) {
        return new DefaultMailService(mailSender, properties, bootMailProperties);
    }
}
