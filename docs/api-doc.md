# 接口文档（Knife4j / SpringDoc）

`ycr-starter-api-doc` 集成 SpringDoc OpenAPI 与 Knife4j，自动装配文档信息。引入后访问 `/doc.html` 查看在线接口文档。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-api-doc</artifactId>
</dependency>
```

## 配置

前缀 `ycr.api-doc`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.api-doc.enabled` | `true` | 是否启用文档 |
| `ycr.api-doc.title` | `API 文档` | 文档标题 |
| `ycr.api-doc.description` | `""` | 描述 |
| `ycr.api-doc.version` | `1.0.0` | 版本 |
| `ycr.api-doc.contact-name` | `""` | 联系人 |
| `ycr.api-doc.contact-email` | `""` | 联系邮箱 |

```yaml
ycr:
  api-doc:
    title: 用户服务 API
    version: 2.0.0
```

> 生产环境建议 `ycr.api-doc.enabled: false` 关闭。文档静态资源路径默认已在 `ycr-starter-security` 的放行白名单中（`/doc.html`、`/v3/api-docs/**`、`/webjars/**`）。

## 用法

用标准 SpringDoc 注解描述接口：

```java
@Tag(name = "用户管理")
@RestController
public class UserController {
    @Operation(summary = "创建用户")
    @PostMapping
    public R<Long> create(@RequestBody @Valid UserCreateReq req) { ... }
}
```
