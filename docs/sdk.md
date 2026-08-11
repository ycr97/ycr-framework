# SDK 发布

> 成熟度：**Experimental**。优先直接使用 `ycr-starter-feign`；本模块封装契约在 1.0 前可能调整。

`ycr-starter-sdk` 支持「SDK 发布模式」：服务提供方在 client 模块定义 Feign 接口并封装为 SDK 服务类，消费方引入 jar 即可像本地服务一样注入调用。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-sdk</artifactId>
</dependency>
```

无配置项。

## 提供方：封装 SDK 服务

继承 `AbstractFeignClientService<T>`，`T` 为具体 Feign 接口，由 Spring 泛型感知注入：

```java
@FeignClient(name = "user-service", path = "/api/users")
public interface UserClient {
    @GetMapping("/{id}")
    R<UserDTO> getById(@PathVariable Long id);
}

public class UserSdkService extends AbstractFeignClientService<UserClient> {
    public UserDTO getUser(Long id) {
        return getClient().getById(id).getData();   // getClient() 取注入的 Feign 客户端
    }
}
```

## 消费方：开启扫描

用 `@EnableSdkClients` 扫描 SDK 包（组合自 `@EnableFeignClients`，`basePackages` 经 `@AliasFor` 转发）：

```java
@EnableSdkClients(basePackages = "com.example.user.sdk")
@SpringBootApplication
public class Application { ... }
```

随后注入 SDK 服务直接使用：

```java
@RequiredArgsConstructor
@Service
public class OrderService {
    private final UserSdkService userSdk;
    public void check(Long uid) { UserDTO u = userSdk.getUser(uid); }
}
```

## 与 ycr-starter-feign 的关系

- `ycr-starter-feign`：调用增强（上下文/Trace 透传、错误解码），面向「直接用 `@FeignClient` 接口」。
- `ycr-starter-sdk`：在其之上提供 SDK 封装基类 + 扫描注解，面向「把远程调用包装成可注入的 SDK 服务对外发布」。
