package com.ycr.framework.idempotent.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.idempotent")
public class IdempotentProperties {

    /** 是否启用幂等，默认关闭 */
    private boolean enabled = false;

    /**
     * 幂等键前缀
     */
    private String keyPrefix = "ycr:idempotent";
}
