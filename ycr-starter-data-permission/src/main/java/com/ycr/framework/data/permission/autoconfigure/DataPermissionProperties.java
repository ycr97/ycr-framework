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
     * 是否启用数据权限（SQL 行级过滤拦截器与注解切面），默认关闭。
     *
     * <p>数据权限会改写 SQL，须由应用完成规则和数据范围配置后显式开启；
     * 开启后，受治理表仍保持 fail-closed。</p>
     */
    private boolean enabled = false;

    /**
     * 是否输出每张表实际追加的数据权限条件（含 traceId）的 debug 日志，默认关。
     */
    private boolean logAppliedConditions = false;
}
