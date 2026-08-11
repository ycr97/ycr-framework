package com.ycr.framework.id.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ID 生成配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.id")
public class IdGenerateProperties {

    /** 是否启用默认雪花 ID 生成器 */
    private boolean enabled;

    /** 工作机器ID (0~31) */
    private Long workerId;

    /** 数据中心ID (0~31) */
    private Long datacenterId;
}
