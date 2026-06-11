# Web 统一响应与异常处理

`ycr-starter-web` 提供统一响应包装和全局异常处理。默认开启，Servlet Web 应用引入 starter 后自动生效。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-web</artifactId>
</dependency>
```

## 响应结构

普通 Controller 返回值会被包装为 `R<T>`：

```json
{
  "code": "200",
  "msg": "操作成功",
  "success": true,
  "timestamp": 1781179200000,
  "data": {
    "id": 1,
    "name": "alice"
  }
}
```

`code` 是字符串类型，业务错误码可以使用 `USER_001` 这类非数字编码。

## 配置

```yaml
ycr:
  web:
    response:
      enabled: true
      include-paths:
        - /api/**
      exclude-paths:
        - /api/files/**
        - /actuator/**
```

配置说明：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.web.response.enabled` | `true` | 是否启用统一响应包装 |
| `ycr.web.response.include-paths` | `/**` | 需要包装的路径，Ant pattern |
| `ycr.web.response.exclude-paths` | 空 | 不需要包装的路径，优先级高于 include |

## Controller 示例

```java
@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping("/{id}")
    UserDTO getById(@PathVariable Long id) {
        return new UserDTO(id, "alice");
    }

    @GetMapping("/raw")
    R<UserDTO> raw() {
        return R.ok(new UserDTO(1L, "alice"));
    }
}
```

`getById` 会被自动包装：

```json
{
  "code": "200",
  "msg": "操作成功",
  "success": true,
  "timestamp": 1781179200000,
  "data": {
    "id": 1,
    "name": "alice"
  }
}
```

`raw` 已经返回 `R`，不会重复包装。

## 不会被统一包装的返回值

- `R<?>`
- `ResponseEntity<?>`
- `Resource`
- `byte[]`
- `InputStream`
- `StreamingResponseBody`
- `ResponseBodyEmitter`
- `SseEmitter`
- 命中 `exclude-paths` 的请求

`String` 返回值会被序列化成 JSON 字符串并把响应 `Content-Type` 设置为 `application/json`。

## 业务异常码保留

抛出 `BizException` 时，响应会保留业务错误码：

```java
throw new BizException("USER_001", "用户不存在");
```

响应：

```json
{
  "code": "USER_001",
  "msg": "用户不存在",
  "success": false,
  "timestamp": 1781179200000,
  "data": null
}
```

其他异常处理：

| 异常 | HTTP 状态 | 响应码 |
| --- | --- | --- |
| `BizException` | `400` | 业务异常自身 `code` |
| `SysException` | `500` | `500` |
| 参数校验异常 | `400` | `400` |
| 不支持的请求方法 | `405` | `405` |
| 未知异常 | `500` | `500` |
