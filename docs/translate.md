# 字段翻译

`ycr-starter-translate` 在 JSON 序列化时把「编码字段」翻译为文本，写出到一个同级目标字段，原编码字段保留。常用于枚举值、字典码转可读文本。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-translate</artifactId>
</dependency>
```

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.translate.enabled` | `true` | 是否启用字段翻译 |

## 用法

在编码字段上标 `@Translate`，序列化后 JSON 同时含原字段与翻译字段（默认名为「原字段名 + Name」）：

```java
@Data
public class UserResp {
    @Translate(type = TranslateType.ENUM, key = "com.example.enums.GenderEnum")
    private Integer gender;     // 序列化后额外产出 genderName
}
```

```json
{ "gender": 1, "genderName": "男" }
```

`@Translate` 参数：`type`、`key`、`source`（仅 CUSTOM）、`targetField`（默认 `原字段名 + Name`）。

## 三种翻译源

| `type` | 源 | 说明 |
| --- | --- | --- |
| `ENUM` | 内置，开箱即用 | `key` 为实现 `BaseEnum` 的枚举全限定类名；按 `getValue()` 匹配，翻译为 `getDescription()`。字段本身即枚举实例时可不填 `key` |
| `DICT` | `DictTranslateSource` | `key` 为字典编码；**需应用注册 `DictProvider` Bean** 提供字典数据，否则字典源不激活 |
| `CUSTOM` | 应用自定义 | 注册一个 `TranslateSource` Bean，`@Translate(source = "其 name()")` 选用；关联表翻译等场景用它 |

```java
// DICT 源所需的应用侧扩展点
@Component
public class DbDictProvider implements DictProvider {
    @Override
    public String getLabel(String dictCode, String itemCode) { /* 查库/缓存 */ }
}
```

## 关联示例

`ycr-scaffold-mvc` Example 的 `UserResp.gender` 用 `@Translate(ENUM)`，冒烟测试断言详情 JSON 含 `genderName=男`。
