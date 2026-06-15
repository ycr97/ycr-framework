# 数据防护（脱敏 + XSS）

`ycr-starter-protect` 提供两项数据安全能力：`@JsonMask` 字段脱敏（输出侧）与 XSS 请求过滤（输入侧）。

> 命名说明：本模块聚焦「数据防护」；鉴权拦截（Sa-Token）见 [security 文档](security.md)，二者关注点不同。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-protect</artifactId>
</dependency>
```

Jackson 与 servlet 依赖为 `optional`，由宿主应用提供（Web 应用天然具备）。

---

## 一、字段脱敏 `@JsonMask`

标在 `String` 字段上，JSON 序列化时按规则脱敏。经 Jackson 元注解机制自动生效，**无需任何配置**。

```java
public class UserResp {
    @JsonMask(MaskType.MOBILE_PHONE)
    private String phone;        // 13812345678 -> 138****5678

    @JsonMask(MaskType.EMAIL)
    private String email;        // david@126.com -> d****@126.com

    @JsonMask(value = MaskType.CUSTOM, left = 1, right = 1)
    private String secret;       // abcdef -> a****f
}
```

### 内置类型 `MaskType`

| 类型 | 规则 | 示例 |
| --- | --- | --- |
| `CUSTOM` | 保留左 `left` 位、右 `right` 位 | `a****f` |
| `CHINESE_NAME` | 仅留首字 | `张**` |
| `MOBILE_PHONE` | 留前 3 后 4 | `138****5678` |
| `FIXED_PHONE` | 留前 4 后 2 | `0571****21` |
| `ID_CARD` | 留前 6 后 4 | `110101********1234` |
| `BANK_CARD` | 留前 4 后 4 | `6222********8888` |
| `EMAIL` | 前缀留首字母 | `d****@126.com` |
| `PASSWORD` | 全部脱敏 | `******` |

`@JsonMask` 参数：`value`（类型）、`strategy`（自定义策略，优先级高于 value）、`left`/`right`（CUSTOM 用）、`character`（脱敏符，默认 `*`）。

### 自定义策略

实现 `MaskStrategy` 并在注解引用（可注册为 Spring Bean，否则按无参构造反射实例化）：

```java
@Component
public class IdCardStrategy implements MaskStrategy {
    @Override
    public String mask(String value, char character, int left, int right) { /* ... */ }
}

@JsonMask(strategy = IdCardStrategy.class)
private String idCard;
```

---

## 二、XSS 过滤

`XssFilter` 对请求参数与请求头清理，防止跨站脚本注入。Servlet Web 环境下自动注册。

### 配置

前缀 `ycr.protect.xss`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.protect.xss.enabled` | `true` | 是否启用 |
| `ycr.protect.xss.mode` | `ESCAPE` | 处理模式：`ESCAPE`（转义，无损，推荐）或 `CLEAN`（去标签，有损） |
| `ycr.protect.xss.include-patterns` | `[]` | 仅过滤这些路径（Ant 风格），留空表示全部 |
| `ycr.protect.xss.exclude-patterns` | `[]` | 放行路径（优先级高于 include） |

```yaml
ycr:
  protect:
    xss:
      mode: ESCAPE
      exclude-patterns:
        - /webhook/**
        - /actuator/**
```

### 两种模式

- **`ESCAPE`（默认）**：把 `< > & " ' /` 转为 HTML 实体，**保留原始内容**，渲染时不执行脚本。推荐——无损、无误伤。
- **`CLEAN`**：逐层移除脚本/样式块、事件处理器（`onclick=` 等）、危险协议（`javascript:` 等）与所有标签，**有损**。

> XSS 过滤覆盖请求参数与请求头，作为输入侧的纵深防御；输出渲染时仍应做转义，二者互补而非替代。
