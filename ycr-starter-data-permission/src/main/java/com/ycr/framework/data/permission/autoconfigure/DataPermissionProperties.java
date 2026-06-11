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

    /**
     * 是否启用数据权限（SQL 行级过滤拦截器与注解切面），默认启用。
     *
     * <p>默认 {@code true}：数据权限属限制性能力，默认开启取安全侧，避免漏配导致越权查询。
     * 设为 {@code false} 可整体关闭数据权限链路。</p>
     */
    private boolean enabled = true;
}
