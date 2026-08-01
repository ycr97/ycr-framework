# Starter 自动配置测试矩阵

## 默认开启基础能力

| 模块 | AutoConfiguration | 默认策略 | 关键 Bean | 默认测试 | 关闭测试 | 覆盖测试 |
| --- | --- | --- | --- | --- | --- | --- |
| api-doc | ApiDocAutoConfiguration | 默认开启 | OpenAPI | 有 | 有 | 有 |
| business | BusinessAutoConfiguration | 默认开启 | BizApiAspect | 有 | 有 | 有 |
| cache | CacheAutoConfiguration | 有 Redisson 时开启 | RedisUtils | 有 | 有 | 不适用 |
| cache-jetcache | JetCacheAnnoAutoConfiguration | 默认开启 | JetCache 注解基础设施 | 有 | 有 | 有 |
| captcha | CaptchaAutoConfiguration | 默认开启 | CaptchaService | 有 | 有 | 有 |
| context | ContextAutoConfiguration | 默认开启 | UserContextResolverChain | 有 | 不适用 | 有 |
| context-redis | ContextRedisAutoConfiguration | 有 RedissonClient 时开启 | ContextReplayGuard | 有 | 无依赖时 fail-closed | 有 |
| context-servlet | ContextServletAutoConfiguration | Servlet 环境开启 | ContextFilter | 有 | 非 Web 不装配 | 有 |
| core | CoreAutoConfiguration | 默认开启 | SpringContextHolder | 有 | 不适用 | 不适用 |
| crud | CrudAutoConfiguration | Servlet 环境开启 | CRUD HandlerMapping | 有 | 非 Web 不装配 | 有 |
| data-mp | MybatisPlusAutoConfiguration | 默认开启 | MetaObjectHandler / MybatisPlusInterceptor | 有 | 有 | 有 |
| ddd-core | DddCoreAutoConfiguration | 默认开启 | DomainEventPublisher | 有 | 不适用 | 有 |
| ddd-extension | ExtensionAutoConfiguration | 默认开启 | ExtensionExecutor | 有 | 不适用 | 有 |
| excel | ExcelAutoConfiguration | Servlet 环境开启 | Excel 导出处理器 | 有 | 非 Web 不装配 | 有 |
| feign | FeignAutoConfiguration | 默认开启 | Feign 拦截器 / ErrorDecoder | 有 | 有 | 有 |
| i18n | I18nAutoConfiguration | 默认开启 | LocaleResolver | 有 | 有 | 有 |
| id-generate | IdGenerateAutoConfiguration | 默认开启 | IdGenerator | 有 | 不适用 | 有 |
| json | JacksonAutoConfiguration | 默认开启 | Jackson Module | 有 | 不适用 | 有 |
| log | LogAutoConfiguration | 默认开启 | LogAspect / LogHandler | 有 | 有 | 有 |
| messaging | MessagingAutoConfiguration | 默认开启 | MailService | 有 | 有 | 有 |
| trace | TraceAutoConfiguration | Servlet 环境开启 | TraceFilter | 有 | 有 | 有 |
| translate | TranslateAutoConfiguration | 默认开启 | TranslateManager | 有 | 有 | 有 |
| web | WebAutoConfiguration | Servlet 环境开启 | GlobalExceptionHandler | 有 | 非 Web 不装配 | 有 |

## 显式开启副作用能力

| 模块 | AutoConfiguration | 默认策略 | 关键 Bean | 默认测试 | 关闭测试 | 覆盖测试 |
| --- | --- | --- | --- | --- | --- | --- |
| data-permission | DataPermissionAutoConfiguration | 显式开启 | DataPermissionInterceptor / DataPermissionAspect | 有 | 有 | 有 |
| auth-satoken | SaTokenAuthAutoConfiguration / SaTokenWebAutoConfiguration | 显式开启；开启后端点默认需登录 | SaTokenSessionManager / SaInterceptor / UserContextResolver / AuthorizeAspect | 有 | 有 | 有 |
| auth-satoken-store | SaTokenSessionStoreAutoConfiguration / SaTokenRedisSessionStoreAutoConfiguration | memory 默认；redis 显式选择且 fail-fast | SaTokenDao | 有 | 有 | 有 |
| encrypt | EncryptAutoConfiguration | 显式开启 | EncryptHandler / Lifecycle | 有 | 有 | 有 |
| idempotent | IdempotentAutoConfiguration | 显式开启 | IdempotentAspect | 有 | 有 | 有 |
| protect-xss | XssAutoConfiguration | 显式开启 | XssFilter | 有 | 有 | 有 |
| ratelimiter | RateLimiterAutoConfiguration | 显式开启 | RateLimiterAspect | 有 | 有 | 有 |
| rocketmq | RocketMqAutoConfiguration | 显式开启 | Producer / Consumer 注册器 | 有 | 有 | 有 |
| security | SecurityAutoConfiguration | 切面显式开启 | AuthorizeAspect | 有 | 有 | 有 |
| storage | StorageAutoConfiguration | 显式开启 | FileStorageService | 有 | 有 | 有 |
| tenant | TenantAutoConfiguration | 显式开启 | TenantLineInnerInterceptor | 有 | 有 | 有 |
| cors | CorsAutoConfiguration | 显式开启 | WebMvcConfigurer | 有 | 有 | 不适用 |
| web-response | WebAutoConfiguration | 响应包装显式开启 | UnifiedResponseBodyAdvice | 有 | 有 | 有 |

## 无自动配置

| 模块 | 说明 |
| --- | --- |
| validation | Jakarta Validation SPI 自动发现校验注解，无需 AutoConfiguration |
