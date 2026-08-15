package com.ycr.framework.messaging.mail;

import com.ycr.framework.messaging.autoconfigure.MessagingProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 默认邮件服务实现
 *
 * <p>纯文本用 {@link SimpleMailMessage}，HTML/附件用 {@link MimeMessage} + {@link MimeMessageHelper}。
 * 发件人取 {@code ycr.messaging.mail-from}，留空回退 {@code spring.mail.username}。</p>
 *
 * @author ycr
 */
@Slf4j
public class DefaultMailService implements MailService {

    private final JavaMailSender mailSender;
    private final MessagingProperties properties;
    private final MailProperties bootMailProperties;

    public DefaultMailService(JavaMailSender mailSender, MessagingProperties properties,
                              MailProperties bootMailProperties) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.bootMailProperties = bootMailProperties;
    }

    @Override
    public void sendText(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(resolveFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    @Override
    public void sendHtml(String to, String subject, String html) {
        send(to, subject, html, null, null);
    }

    @Override
    public void sendWithAttachment(String to, String subject, String html,
                                   String attachmentName, InputStreamSource attachment) {
        send(to, subject, html, attachmentName, attachment);
    }

    private void send(String to, String subject, String html, String attachmentName, InputStreamSource attachment) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            boolean multipart = attachment != null;
            MimeMessageHelper helper = new MimeMessageHelper(message, multipart, StandardCharsets.UTF_8.name());
            helper.setFrom(resolveFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            if (multipart) {
                helper.addAttachment(attachmentName, attachment);
            }
        } catch (Exception e) {
            // 构建邮件失败属不可恢复的发送异常，抛出由上层统一处理
            throw new IllegalStateException("邮件构建失败: " + e.getMessage(), e);
        }
        mailSender.send(message);
    }

    /** 发件人：优先配置值，留空回退 spring.mail.username */
    private String resolveFrom() {
        return StringUtils.hasText(properties.getMailFrom())
                ? properties.getMailFrom()
                : bootMailProperties.getUsername();
    }
}
