package com.ycr.framework.data.permission.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据权限配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.data.permission")
public class DataPermissionProperties {

    /** 是否启用数据权限（SQL 行级过滤拦截器与注解切面），默认关闭 */
    private boolean enabled = false;
}
