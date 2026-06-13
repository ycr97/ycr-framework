# 操作日志

`ycr-starter-log` 通过 AOP 采集 Controller 方法的操作日志（描述、模块、请求信息、耗时、操作人、异常），交给 `LogHandler` 处理。默认输出到 SLF4J。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-log</artifactId>
</dependency>
```

## 配置

前缀 `ycr.log`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.log.enabled` | `true` | 是否启用操作日志切面 |
| `ycr.log.async` | `false` | 是否异步落库（操作人/请求信息在方法执行前已采集，异步不丢身份） |
| `ycr.log.includes` | `[IP_ADDRESS]` | 全局默认采集项 |
| `ycr.log.sensitive-keys` | `password,pwd,idCard,email,phone` | 敏感参数键名（不区分大小写），命中脱敏为 `******` |

采集项 `Include`：`IP_ADDRESS`（客户端 IP）、`REQUEST_PARAMS`（请求参数，敏感键已脱敏）。

## 用法

在方法或类上标 `@Log`：

```java
@Log(value = "创建用户", module = "用户管理")
@PostMapping
public R<Long> create(@RequestBody @Valid UserCreateReq req) { ... }
```

`@Log` 属性：

| 属性 | 说明 |
| --- | --- |
| `value` | 日志描述，空时回退取方法名 |
| `module` | 所属模块；方法未指定时回退取类级 `@Log` 的模块 |
| `includes` / `excludes` | 在全局 `includes` 基础上增 / 减采集项 |
| `ignore` | `true` 时忽略该方法日志 |

```java
@Log(value = "导出用户", module = "用户管理", includes = Include.REQUEST_PARAMS)
```

## 自定义处理（落库等）

默认 `Slf4jLogHandler` 打印日志。注册自己的 `LogHandler` Bean 即可改为落库/上报，入参为采集好的 `LogRecord`（含 `description/module/requestUrl/status/elapsedTime/clientIp/operatorId/operatorName/operateTime` 等）：

```java
@Component
public class DbLogHandler implements LogHandler {
    @Override
    public void handle(LogRecord record) { /* 入库 */ }
}
```

## 关联示例

`ycr-scaffold-mvc` Example 的 `UserController` 各写操作标 `@Log`，测试运行时可见操作日志输出。
