# JSON（Jackson 扩展）

`ycr-starter-json` 在 Spring Boot 默认 Jackson 之上做两项增强，并提供静态 `JsonUtils`。引入即自动生效。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-json</artifactId>
</dependency>
```

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.json.big-number-to-string` | `true` | 大数（`Long`/`BigInteger`/`BigDecimal`）序列化为字符串，规避前端 JS 精度丢失（雪花 ID 等长整型必备） |

## 大数转字符串

开启后，长整型在 JSON 中以字符串输出：

```json
{ "id": "2065682536556257282", "amount": "1000.00" }
```

## 枚举序列化

实现 `BaseEnum`（见 [core 文档](core.md)）的枚举：序列化输出其 `getValue()` 编码，反序列化时按编码还原枚举实例。前后端按编码交互，描述文本另由 `ycr-starter-translate` 产出。

## JsonUtils

```java
String json = JsonUtils.toJson(obj);
User user   = JsonUtils.fromJson(json, User.class);
List<User> list = JsonUtils.fromJson(json, new TypeReference<List<User>>() {});
```

复用容器内配置好的 `ObjectMapper`（含上述扩展），保证与 Web 序列化行为一致。
