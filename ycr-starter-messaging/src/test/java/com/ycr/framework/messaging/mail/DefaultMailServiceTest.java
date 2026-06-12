package com.ycr.framework.messaging.mail;

import com.ycr.framework.messaging.autoconfigure.MessagingProperties;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DefaultMailService 行为测试（mock JavaMailSender）
 *
 * @author ycr
 */
class DefaultMailServiceTest {

    private DefaultMailService build(JavaMailSender sender, String mailFrom, String bootUsername) {
        MessagingProperties props = new MessagingProperties();
        props.setMailFrom(mailFrom);
        MailProperties bootProps = new MailProperties();
        bootProps.setUsername(bootUsername);
        return new DefaultMailService(sender, props, bootProps);
    }

    @Test
    void 发送纯文本应带正确字段() {
        JavaMailSender sender = mock(JavaMailSender.class);
        DefaultMailService service = build(sender, "noreply@ycr.com", "sys@ycr.com");

        service.sendText("a@b.com", "主题", "正文");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertEquals("noreply@ycr.com", msg.getFrom());
        assertArrayEquals(new String[]{"a@b.com"}, msg.getTo());
        assertEquals("主题", msg.getSubject());
        assertEquals("正文", msg.getText());
    }

    @Test
    void mailFrom留空应回退spring用户名() {
        JavaMailSender sender = mock(JavaMailSender.class);
        DefaultMailService service = build(sender, "", "sys@ycr.com");

        service.sendText("a@b.com", "主题", "正文");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        assertEquals("sys@ycr.com", captor.getValue().getFrom());
    }

    @Test
    void 发送HTML应提交MimeMessage() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mime = new MimeMessage((jakarta.mail.Session) null);
        when(sender.createMimeMessage()).thenReturn(mime);
        DefaultMailService service = build(sender, "noreply@ycr.com", "sys@ycr.com");

        service.sendHtml("a@b.com", "HTML主题", "<b>hi</b>");

        verify(sender).send(mime);
        assertEquals("HTML主题", mime.getSubject());
    }

    @Test
    void 发送带附件应提交MimeMessage() {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mime = new MimeMessage((jakarta.mail.Session) null);
        when(sender.createMimeMessage()).thenReturn(mime);
        DefaultMailService service = build(sender, "noreply@ycr.com", "sys@ycr.com");

        service.sendWithAttachment("a@b.com", "带附件", "<b>见附件</b>",
                "f.txt", new ByteArrayResource("hello".getBytes()));

        verify(sender).send(mime);
    }
}
