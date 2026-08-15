package com.ycr.framework.tenant.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 多租户配置
 *
 * <p>跳过租户过滤的三种方式：表级用 {@link #ignoreTables} 配置；方法级用 MyBatis-Plus 原生
 * {@code @InterceptorIgnore(tenantLine = "1")} 标注 Mapper 方法；运行期作用域用
 * {@code TenantHelper.run/call}（适合定时任务、登录前、系统级跨租户操作）。本模块不另造自定义注解。</p>
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.tenant")
public class TenantProperties {

    /** 是否启用多租户隔离，默认关闭（多租户属高风险能力，须显式开启 fail-closed） */
    private boolean enabled = false;

    /** 租户 ID 列名 */
    private String tenantIdColumn = "tenant_id";

    /** 忽略租户过滤的表名（如系统配置、字典等全局表） */
    private List<String> ignoreTables = new ArrayList<>();
}
