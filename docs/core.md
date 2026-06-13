# 核心基础（响应 / 异常 / 枚举 / 工具）

`ycr-starter-core` 是所有 starter 的基础依赖，提供统一响应体、异常体系、枚举契约与基础工具。通常无需显式引入（被其他 starter 传递依赖）。

## 统一响应 `R<T>`

```java
R.ok();                       // 成功，无数据
R.ok(data);                   // 成功，带数据
R.ok("自定义消息", data);
R.fail("业务码", "消息");
R.fail(404, "未找到");
R.fail("操作失败");
```

字段：`code`（字符串，成功为 `"200"`）、`msg`、`success`、`timestamp`、`data`。配合 `ycr-starter-web` 可自动包装 Controller 返回值（见 [web 文档](web-response.md)）。

## 异常体系

| 类 | 语义 | HTTP |
| --- | --- | --- |
| `BaseException` | 异常基类（携带 `ErrorCode`） | — |
| `BizException` | 业务异常（可预期，如校验/状态不满足） | 400 |
| `SysException` | 系统异常（不可预期） | 500 |

错误码实现 `ErrorCode` 接口（`getCode()` / `getMessage()`），通常用枚举集中定义：

```java
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("USER_001", "用户不存在");
    private final String code;
    private final String message;
}

throw new BizException(UserErrorCode.USER_NOT_FOUND);
```

由 `ycr-starter-web` 的全局异常处理器统一兜底并透出业务码。

## 枚举契约 `BaseEnum<T>`

```java
public enum GenderEnum implements BaseEnum<Integer> {
    MALE(1, "男"), FEMALE(2, "女");
    // getValue() 返回编码，getDescription() 返回文本
}
```

实现后即可被 `ycr-starter-json`（序列化为 `value`）、`ycr-starter-translate`（`@Translate(ENUM)`）、`ycr-starter-validation`（`@EnumValue`）识别。`BaseEnum.getByValue(value, clazz)` 按编码反查枚举。

## 工具类

- `SpringContextHolder` —— 静态获取容器与 Bean：`getBean(Class)` / `getBean(name)` / `getContext()`（由 `CoreAutoConfiguration` 注册）。
- `ServletUtils` —— `getRequest()` / `getResponse()`，返回 `Optional`。
