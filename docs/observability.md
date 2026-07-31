# 可观测性标准

YCR Framework 为同步 HTTP 链路提供统一请求标识、上下文 MDC、慢请求日志、操作审计和结构化异常日志。

## MDC 字段

| 字段 | 来源 | 生命周期 |
| --- | --- | --- |
| `traceId` | `X-Trace-Id`，缺失时自动生成 | 整个请求链路 |
| `requestId` | `X-Request-Id`，缺失时自动生成 | 单次请求 |
| `userId` | 已验证的 `UserContext` | 用户上下文还原后至请求结束 |
| `tenantId` | 已验证的 `UserContext` | 用户上下文还原后至请求结束 |
| `clientId` | 已验证的 `UserContext` | 用户上下文还原后至请求结束 |

`TraceUtils.wrap(Runnable/Callable)` 会复制完整 MDC，并在任务结束后恢复执行线程原有上下文。

## 配置

```yaml
ycr:
  trace:
    enabled: true
    header-name: X-Trace-Id
    request-header-name: X-Request-Id
    slow-request:
      enabled: true
      threshold-ms: 1000
```

慢请求日志默认开启，阈值为 `1000ms`，事件名为 `slow_request`，包含 method、uri、status、elapsedMs 和完整 MDC 标识。

## 操作日志

操作日志在业务线程内、异步分发前复制以下审计字段：

- `traceId`
- `tenantId`
- `clientId`
- `operatorId`
- `operatorName`

默认日志事件名为 `operation_log`。业务方实现 `LogHandler` 异步落库时，应直接使用 `LogRecord` 中已复制的字段，不要在异步线程重新读取 Holder 或 MDC。

## 异常事件

| 事件 | 场景 |
| --- | --- |
| `biz_exception` | 业务异常和鉴权异常 |
| `validation_exception` | 参数绑定和校验异常 |
| `system_exception` | 已分类的系统异常，保留堆栈 |
| `unexpected_exception` | 未知异常，保留堆栈 |

异常日志统一包含 traceId、requestId、userId、tenantId、HTTP 状态和错误信息，便于日志平台按稳定键检索和告警。
