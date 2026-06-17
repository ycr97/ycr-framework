# 通用纯类库（ycr-common）

`ycr-common` 是**零自动配置**的纯 Java 类库：不注册任何 Spring Bean，引入即用。收纳跨模块复用的工具、校验分组与通用 DTO。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-common</artifactId>
</dependency>
```

## 内容

### 树构建 `TreeUtils`

把扁平列表组装成父子嵌套树。父节点不在集合中的节点会被当作顶层，避免数据丢失；孤儿节点也会按传入的 `Comparator` 重排。

```java
// 节点继承 BaseTreeDTO<自身类型, 主键类型>
class DeptNode extends BaseTreeDTO<DeptNode, Long> { /* 业务字段 */ }

List<DeptNode> tree = TreeUtils.parseTree(flatList, null);           // 按 id/parentId
List<RegionNode> byCode = TreeUtils.parseTreeByCode(flatList, null); // 按 code/parentCode
```

- 第二参数为同层排序 `Comparator`，传 `null` 时：`parseTree` 按 id 数值升序，`parseTreeByCode` 不额外排序。
- 叶子节点的 `children` 为 `null`。

### 校验分组 `valid`

配合 `@Validated` 分场景校验：`AddGroup` / `UpdateGroup` / `DeleteGroup`。

```java
public class UserDTO {
    @Null(groups = AddGroup.class)
    @NotNull(groups = UpdateGroup.class)
    private Long id;
}
// controller: public void save(@Validated(AddGroup.class) @RequestBody UserDTO dto)
```

### 通用 DTO

`IdDto`：仅含 `@NotNull Long id`，用于按 id 的简单入参。`BaseTreeDTO` / `BaseTreeCodeDTO`：树节点基类。
