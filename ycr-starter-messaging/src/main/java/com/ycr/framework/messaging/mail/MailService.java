package com.ycr.framework.messaging.mail;

import org.springframework.core.io.InputStreamSource;

/**
 * 邮件服务
 *
 * <p>叠在 Spring 第一方 {@code JavaMailSender} 之上。业务方可实现此接口替换实现，框架通过
 * {@code @ConditionalOnMissingBean} 让自定义实现覆盖默认实现。</p>
 *
 * @author ycr
 */
public interface MailService {

    /**
     * 发送纯文本邮件
     */
    void sendText(String to, String subject, String content);

    /**
     * 发送 HTML 邮件
     */
    void sendHtml(String to, String subject, String html);

    /**
     * 发送带单个附件的 HTML 邮件
     *
     * @param attachmentName 附件文件名
     * @param attachment     附件内容（如 {@code ByteArrayResource}/{@code FileSystemResource}）
     */
    void sendWithAttachment(String to, String subject, String html, String attachmentName, InputStreamSource attachment);
}
