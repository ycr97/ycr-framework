package com.ycr.framework.data.mp.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis-Plus 扩展配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.data.mp")
public class MybatisPlusProperties {

    private boolean autoFillEnabled = true;

    private boolean paginationEnabled = true;

    private Long maxLimit = 1000L;
}
