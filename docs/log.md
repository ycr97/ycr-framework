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
| `ycr.log.max-body-length` | `2000` | 请求体/响应体序列化截断上限（字符） |
| `ycr.log.method.enabled` | `true` | 是否装配方法调用日志切面（级别默认 DEBUG，生产默认静默） |
| `ycr.log.method.level` | `DEBUG` | 方法调用日志打印级别（DEBUG/INFO） |
| `ycr.log.method.max-length` | `2000` | 方法调用日志入参/出参截断上限（字符） |

采集项 `Include`：`IP_ADDRESS`（客户端 IP）、`REQUEST_PARAMS`（请求参数，脱敏）、`REQUEST_BODY`（请求体，脱敏截断）、`RESPONSE_BODY`（响应体，脱敏截断）、`REQUEST_HEADERS`（请求头，Authorization/Cookie/Set-Cookie 强制脱敏）、`BROWSER`、`OS`（User-Agent 解析）、`IP_REGION`（IP 归属地，经 `IpRegionResolver` SPI）。

> 响应体采集的是 Controller 返回对象（`UnifiedResponseBodyAdvice` 包 `R<T>` 之前的业务载荷）。body 序列化经 `LogJsonSupport`，对字段名命中 `sensitive-keys` 的值递归脱敏并按 `max-body-length` 截断；非 web 应用无 `ObjectMapper` 时静默降级，不影响业务。

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

## IP 归属地（IpRegionResolver SPI）

开启 `IP_REGION` 采集后，框架调用 `IpRegionResolver` 解析归属地。L1 默认 no-op（返回 null），业务在 L2/L3 用 ip2region 等实现并注册为 Bean 覆盖：

```java
@Component
public class Ip2RegionResolver implements IpRegionResolver {
    @Override
    public String resolve(String ip) {
        return searcher.search(ip);   // 业务自带 ip2region 数据
    }
}
```

## 方法调用日志（@MethodLog）

开发排障型，自动把方法入参/出参/耗时/异常打到 SLF4J，**与审计 `@Log` 彻底分离**（不落库）：

```java
@MethodLog("结算")
public Result settle(SettleCmd cmd) { ... }
```

双控：`ycr.log.method.enabled`（装配开关）+ 日志级别（`ycr.log.method.level`，默认 DEBUG）。级别未开启时连序列化都跳过，生产默认静默零开销。入参/出参经 `LogJsonSupport` 脱敏截断。`@MethodLog` 属性：`value`（描述）、`args`（默认打印入参）、`result`（默认打印出参）。

## 关联示例

`ycr-scaffold-mvc` Example 的 `UserController` 各写操作标 `@Log`，测试运行时可见操作日志输出。
