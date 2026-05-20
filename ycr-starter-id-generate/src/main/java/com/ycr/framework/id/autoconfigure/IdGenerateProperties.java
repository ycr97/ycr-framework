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

    /** 工作机器ID (0~31) */
    private long workerId = 1;

    /** 数据中心ID (0~31) */
    private long datacenterId = 1;
}
