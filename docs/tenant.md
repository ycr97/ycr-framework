# 多租户

`ycr-starter-tenant` 基于 MyBatis-Plus 租户插件，对 SQL 自动追加 `tenant_id` 条件实现行级租户隔离。**默认关闭**，需显式开启。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-tenant</artifactId>
</dependency>
```

## 配置

前缀 `ycr.tenant`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.tenant.enabled` | `false` | 是否启用多租户（opt-in） |
| `ycr.tenant.tenant-id-column` | `tenant_id` | 租户列名 |
| `ycr.tenant.ignore-tables` | `[]` | 不做租户隔离的表（如全局字典表） |

```yaml
ycr:
  tenant:
    enabled: true
    ignore-tables:
      - sys_dict
      - sys_config
```

## 工作机制

租户 ID 取自 `TenantContextHolder`（见 [context 文档](context.md)）。**fail-closed**：当租户上下文为空时抛异常拒绝执行，避免漏注入租户条件导致的越权全表查询。`ignore-tables` 中的表跳过隔离。

## 临时旁路（TenantHelper）

为定时任务、登录前流程、系统级跨租户操作提供「作用域内临时关闭租户隔离」的逃生口（基于计数器 ThreadLocal，支持嵌套，`run`/`call` 已保证成对退出）：

```java
TenantHelper.run(() -> {
    // 此作用域内不注入租户条件，也不触发 fail-closed
    sysJobMapper.scanAllTenants();
});

List<Tenant> all = TenantHelper.call(() -> tenantMapper.selectList(null));

boolean ignoring = TenantHelper.isIgnored();   // 当前是否处于旁路作用域
```

> 与 `ignore-tables`（静态表级忽略）的区别：`TenantHelper` 是**运行时按作用域**临时旁路，粒度更细、更可控。
