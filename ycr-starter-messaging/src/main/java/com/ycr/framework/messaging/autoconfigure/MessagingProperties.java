package com.ycr.framework.messaging.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 消息通知配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.messaging")
public class MessagingProperties {

    /** 是否启用邮件通道，默认启用（仍需配置 spring.mail.host 才会真正装配） */
    private boolean mailEnabled = true;

    /** 默认发件人；留空则回退取 spring.mail.username，避免重复配置 */
    private String mailFrom = "";
}
