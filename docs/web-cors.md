# Web CORS 跨域配置

`ycr-starter-web` 提供基于 Spring MVC 的 CORS 自动配置。该能力默认关闭，须由应用显式开启并配置可信来源。

## 配置

前缀 `ycr.web.cors`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.web.cors.enabled` | `false` | 是否启用 CORS，须显式开启 |
| `ycr.web.cors.allowed-origins` | `["*"]` | 允许的来源模式 |
| `ycr.web.cors.allowed-methods` | `GET,POST,PUT,DELETE,OPTIONS` | 允许的 HTTP 方法 |
| `ycr.web.cors.allowed-headers` | `["*"]` | 允许的请求头 |
| `ycr.web.cors.allow-credentials` | `false` | 是否允许携带凭证 |
| `ycr.web.cors.max-age` | `3600` | 预检请求缓存秒数 |

生产环境应列出明确来源：

```yaml
ycr:
  web:
    cors:
      enabled: true
      allowed-origins:
        - https://console.example.com
        - https://app.example.com
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowed-headers:
        - Authorization
        - Content-Type
        - X-Request-Id
      allow-credentials: true
      max-age: 3600
```

启用凭证时不要配置通配来源，避免浏览器策略冲突和不必要的跨域暴露。
