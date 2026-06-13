# 链路追踪（TraceId）

`ycr-starter-trace` 为每个请求生成/透传 TraceId，写入日志 MDC，实现单服务内与跨服务的链路串联。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-trace</artifactId>
</dependency>
```

## 配置

前缀 `ycr.trace`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.trace.enabled` | `true` | 是否启用链路追踪 |
| `ycr.trace.header-name` | `X-Trace-Id` | TraceId 透传头名称（与 Feign 透传统一来源） |
| `ycr.trace.filter-order` | `HIGHEST_PRECEDENCE` | 过滤器排序，置于最外层确保最先设置、最后清理 |

## 工作机制

`TraceFilter` 在请求入口：优先取上游 `X-Trace-Id` 头，无则生成一个，存入 MDC（键 `traceId`），请求结束清理。在日志格式中引用 `%X{traceId}` 即可打印：

```
logging.pattern.level: "[%X{traceId}] %5p"
```

## TraceUtils

```java
String tid = TraceUtils.getTraceId();
TraceUtils.setTraceId(tid);
TraceUtils.removeTraceId();
String newId = TraceUtils.generateTraceId();
```

**线程池透传**：异步任务默认丢失 MDC 中的 traceId，用 `wrap` 包装可携带：

```java
executor.submit(TraceUtils.wrap(() -> doWork()));            // Runnable
Future<T> f = executor.submit(TraceUtils.wrap(callable));    // Callable
```

## 跨服务透传

配合 `ycr-starter-feign`：调用下游时拦截器自动把当前 traceId 写入 `X-Trace-Id` 头，下游 `TraceFilter` 还原，实现全链路同一 traceId。
