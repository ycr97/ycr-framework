# 消息（邮件）

`ycr-starter-messaging` 提供邮件发送服务 `MailService`（文本/HTML/带附件），基于 Spring Mail。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-messaging</artifactId>
</dependency>
```

## 配置

前缀 `ycr.messaging`，邮件连接走 Spring 标准 `spring.mail.*`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.messaging.mail-enabled` | `true` | 是否启用邮件服务 |
| `ycr.messaging.mail-from` | `""` | 默认发件人 |

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 465
    username: noreply@example.com
    password: ${MAIL_PASSWORD}
ycr:
  messaging:
    mail-from: noreply@example.com
```

## 用法

注入 `MailService`：

```java
@RequiredArgsConstructor
@Service
public class NotifyService {
    private final MailService mailService;

    public void notify() {
        mailService.sendText("to@x.com", "标题", "纯文本内容");
        mailService.sendHtml("to@x.com", "标题", "<h1>HTML 内容</h1>");
        mailService.sendWithAttachment("to@x.com", "标题", "<p>见附件</p>",
                "report.xlsx", new InputStreamResource(in));
    }
}
```
