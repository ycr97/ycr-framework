package com.ycr.framework.ratelimiter.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.ratelimiter")
public class RateLimiterProperties {

    /** 是否启用限流，默认关闭 */
    private boolean enabled = false;

    /**
     * 限流键前缀
     */
    private String keyPrefix = "ycr:ratelimiter";
}
