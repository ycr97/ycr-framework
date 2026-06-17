# 校验约束扩展

`ycr-starter-validation` 提供两个 Jakarta Bean Validation 自定义约束。无自动配置——约束通过 Jakarta Validation SPI 自动发现，引入依赖即可在被 `@Valid`/`@Validated` 触发的校验中生效。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-validation</artifactId>
</dependency>
```

## @Mobile —— 手机号

```java
public class UserCreateReq {
    @Mobile
    private String phone;                       // 默认消息「手机号格式不正确」
}
```

## @EnumValue —— 取值白名单

校验字段值是否落在给定的整数或字符串集合内（不依赖具体枚举类型，直接列举允许值）：

```java
@EnumValue(intValues = {0, 1, 2}, message = "性别取值非法")
private Integer gender;

@EnumValue(strValues = {"A", "B", "C"})
private String level;
```

## 用法

约束随标准校验流程触发，Controller 入参配合 `@Valid` 即可：

```java
@PostMapping
public R<Long> create(@RequestBody @Valid UserCreateReq req) { ... }
```

校验失败由 `ycr-starter-web` 的全局异常处理器统一转为 `R` 响应。

## @ForbiddenContent 非法内容校验

拒绝包含 XSS 脚本、SQL 注入、爬虫、python 代码特征的字符串，配合 `@Valid` 触发：

```java
public class CommentDTO {
    @ForbiddenContent(message = "评论包含非法内容")
    private String content;
}
```

null/空白视为合法，应另用 `@NotBlank` 管控必填。
