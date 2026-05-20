package com.ycr.framework.cache.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存配置属性
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.cache")
public class CacheProperties {

    private boolean enabled = true;
}
