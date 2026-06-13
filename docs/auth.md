# 认证（Sa-Token 集成）

`ycr-starter-auth` 集成 Sa-Token，提供登录态与框架用户上下文双向联动的 `LoginHelper`，以及 Sa-Token 异常的统一处理。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-auth</artifactId>
</dependency>
```

依赖 `ycr-starter-context` 的用户上下文模型。

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.auth.exclude-paths` | `[]` | 放行路径列表（无需登录即可访问） |

```yaml
ycr:
  auth:
    exclude-paths:
      - /api/login
      - /api/captcha
```

Sa-Token 自身配置仍走其原生 `sa-token.*` 前缀。

## LoginHelper

封装 Sa-Token 登录态与框架 `UserContext`：

```java
// 登录：签发 token、写入 Sa 会话、填充当前线程上下文（userId 必填）
LoginHelper.login(userContext);

boolean logged = LoginHelper.isLogin();
String token   = LoginHelper.getTokenValue();
UserContext c  = LoginHelper.getUserContext();   // 线程无上下文时从 Sa 会话懒还原
Long uid       = LoginHelper.getUserId();
String name    = LoginHelper.getUsername();

LoginHelper.logout();        // 注销登录态并清理上下文
```

## 异常处理

`SaTokenExceptionHandler` 将未登录、无权限等 Sa-Token 异常转换为统一 `R` 响应，与 `ycr-starter-web` 的全局异常体系一致。

## 注意

- `login` 以 `userId` 作为 Sa-Token 登录主体，并把完整 `UserContext` 存入会话键 `ycr_user_context`。
- 直接凭 token 调用（未经 `ContextFilter` 还原）时，`getUserContext()` 会从会话懒加载还原，无需手动设置。
