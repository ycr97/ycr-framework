# Excel 导出

`ycr-starter-excel` 基于 FastExcel，提供注解式零样板导出：Controller 方法返回 `List<T>` 并标 `@ExcelExport`，由返回值处理器自动写出 xlsx 到响应（浏览器下载）。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-excel</artifactId>
</dependency>
```

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `ycr.excel.enabled` | `true` | 是否启用 Excel 导出（注册返回值处理器与转换器） |

## 用法

模型字段用 FastExcel 的 `@ExcelProperty` 标列名；导出方法返回 `List<T>` 并标 `@ExcelExport`：

```java
public class UserResp {
    @ExcelProperty("用户名") private String username;
    @ExcelProperty("性别")   private String genderLabel;
}
```

```java
@ExcelExport(value = "用户列表", sheetName = "用户")
@GetMapping("/export")
public List<UserResp> export(UserQuery query) {
    return userService.listForExport(query);   // 直接返回 List，无需手写 response
}
```

`@ExcelExport` 参数：`value`（文件名，不含后缀，默认 `export`）、`sheetName`（默认 `Sheet1`）。导出自动启用列宽自适应。

## 手动导出与大数

需要在方法体内控制时，直接用 `ExcelUtils.export(...)`。Long/BigDecimal 等大数字段为防 Excel 精度丢失，在字段上指定转换器：

```java
@ExcelProperty(value = "ID", converter = ExcelBigNumberConverter.class)
private Long id;
```

枚举字段可用 `ExcelBaseEnumConverter`。

## 注意

- `@ExcelExport` 仅适用于 Servlet Web 环境，且方法须返回带具体元素类型的 `List<T>`（处理器从泛型解析列模型）。
- 注解导出会接管响应渲染，方法返回后不再走视图解析。

## 关联示例

`ycr-scaffold-mvc` Example 的 `UserController.export`（手写分层 + `@ExcelExport`）。
