# 通用 CRUD 自动端点

> 成熟度：**Experimental**。仅建议用于经过评审的低复杂度内部后台，不进入默认生产底座依赖。

`ycr-starter-crud` 提供 `AbstractCrudController`，继承即获得一套增删改查 + 分页 + 列表 REST 端点，免写样板。基于 `ycr-starter-data-mp`。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-crud</artifactId>
</dependency>
```

CRUD 自动端点默认关闭，必须显式启用：

```yaml
ycr:
  crud:
    enabled: true
```

端点治理（`@CrudApi`）通过 Spring Boot 的 `WebMvcRegistrations` 扩展点生效，自动配置仅在 Servlet Web 应用激活。
若应用自定义 `WebMvcRegistrations`，其 `getRequestMappingHandlerMapping()` 必须返回
`CrudApiRequestMappingHandlerMapping`；否则应用启动失败，避免声明关闭的端点被静默重新暴露。

## 用法

三步：实体 `extends BaseDO`、声明一个 `CrudService` Bean、控制器继承基类。

```java
// 1. 后端服务 Bean：绑定具体 Mapper
@Service
public class UserCrudService extends CrudServiceImpl<UserMapper, UserDO, Long> {
}

// 2. 控制器：泛型为 <实体, 主键, 查询对象>
@RestController
@RequestMapping("/api/users")
public class UserCrudController extends AbstractCrudController<UserDO, Long, UserQuery> {
}
```

查询对象 `UserQuery` 用 `@Query` 标注字段（见 `ycr-starter-data-mp`）。即获端点：

| 方法 | 路径 | 说明 | `Api` 枚举 |
| --- | --- | --- | --- |
| GET | `/page` | 分页查询（额外接 `PageQuery` 参数） | `PAGE` |
| GET | `/list` | 列表查询 | `LIST` |
| GET | `/{id}` | 详情 | `GET` |
| POST | `/` | 新增 | `CREATE` |
| PUT | `/` | 修改 | `UPDATE` |
| DELETE | `/{id}` | 删除 | `DELETE` |

返回值统一包装为 `R<T>`；增删改返回 `R<Boolean>`（影响行数 > 0 为 `true`，避免 id 不存在时的假成功）。

## 关闭部分端点

```java
@CrudApi(disable = {Api.DELETE, Api.UPDATE})   // 标在控制器子类上，被关端点返回 404
@RestController
@RequestMapping("/api/users")
public class UserCrudController extends AbstractCrudController<UserDO, Long, UserQuery> {
}
```

## 注意

- **薄 DO 直通**：请求体与响应体直接是实体 `T`，不经独立 DTO 转换。需要 DTO 校验、字段裁剪、复杂编排时，请改用手写分层 Controller→Service（参见 `ycr-scaffold-mvc` Example 的 `UserController`）。
- 覆写基类方法即可定制单个端点行为。

## 关联示例

`ycr-scaffold-mvc` Example 的 `UserCrudController` + `UserCrudService`（`/api/v2/users`）。
