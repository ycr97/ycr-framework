package com.ycr.framework.mq.rocketmq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RocketMQ 配置。
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.mq.rocketmq")
public class RocketMqProperties {

    /** 是否启用 RocketMQ，默认关闭 */
    private boolean enabled = false;

    /** 接入点 endpoints */
    private String endpoints;

    /** AccessKey */
    private String accessKey;

    /** SecretKey */
    private String secretKey;

    /** 默认消费者组（注解未指定 group 时使用） */
    private String group;

    /** 环境后缀，非空时 topic/group 追加 "_env" */
    private String env;

    /** 生产者最大重试次数 */
    private int maxAttempts = 3;
}
