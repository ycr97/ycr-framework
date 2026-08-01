# YCR Framework 0.9.0-RC2 B0 验证矩阵

## 验证范围

- 验证日期：2026-08-01
- RC2 基线：`v0.9.0-RC2` → `0ab88cecbe1c06ce891e72af6435a777d5bbb2dc`
- 验证分支：`main`
- 验证方式：仓内集成测试 + `/private/tmp/ycr-rc2-b0` 外部消费者独立 JVM HTTP 验证
- Part C OAuth2 Resource Server：未开始

验证过程中未停止现有 Redis、未执行 `FLUSHDB`，所有认证域使用随机值；临时应用节点通过测试 teardown 清理，登录态通过登出或随机认证域隔离。

## 矩阵结果

| 场景 | 验证方式 | 结果 |
| --- | --- | --- |
| 单实例开发：Sa-Token + memory | `SaTokenAuthWebIntegrationTest`，覆盖登录、私有端点、权限、登出失效 | 2 tests，0 failures，0 errors，0 skipped |
| 单实例生产模拟：Sa-Token + Redis + auth-domain | B0 独立 JVM 节点启动、登录后停止并以相同认证域重启，原 token 继续访问 | 通过 |
| 双实例 | 节点 A 登录，节点 B 使用相同 `auth-domain` 恢复 `UserContext` | 通过 |
| 应用隔离 | 两节点共享 Redis，使用不同 `auth-domain`；A token 在 B 返回 401 | 通过 |
| 显式 SSO 域 | 两节点明确配置相同 `auth-domain`，跨节点访问成功 | 通过 |
| CORS | `SaTokenAuthWebIntegrationTest` 验证合法 Origin preflight 通过，未登录实际业务请求仍受保护 | 通过 |
| Redis 故障 | B0 节点使用未监听的 `127.0.0.1:6399`，应用因 `RedisConnectionException` 启动失败 | 通过，fail-closed |
| 滚动发布 | 节点 A 登录，节点 B 恢复；停止 A 后以相同认证域启动替代节点，原 token 和 session 继续有效 | 通过 |

## 执行证据

### 仓内 Web 与 Redis 集成测试

```text
mvn -q -pl ycr-starter-auth-satoken -am \
  -Dtest=SaTokenAuthWebIntegrationTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

结果：2 tests，0 failures，0 errors，0 skipped。

```text
YCR_REDIS_INTEGRATION_TESTS=true \
YCR_TEST_REDIS_ADDRESS=redis://127.0.0.1:6379 \
mvn -q -pl ycr-starter-auth-satoken -am \
  -Dtest=SaTokenRedisIntegrationTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

结果：2 tests，0 failures，0 errors，0 skipped。该测试验证共享 token、TTL、`UserContext` 序列化恢复和删除，使用随机 key 并 finally 清理。

### 外部消费者 B0 topology smoke test

临时工程：`/private/tmp/ycr-rc2-b0`。该工程通过 RC2 BOM 消费 `ycr-starter-web`、`ycr-starter-auth-satoken` 和 Redisson，测试启动真实独立 JVM 应用节点，并通过 HTTP 验证业务行为：

```text
mvn -q test
```

结果：3 tests，0 failures，0 errors，0 skipped。

覆盖的独立 JVM 场景：

1. 相同认证域的两个节点共享登录态、权限校验通过，并在节点替换后恢复原 token；
2. 不同认证域隔离 token；
3. Redis 不可用时应用启动 fail-closed，不回退到 memory。

验证前后 `redis-cli -h 127.0.0.1 -p 6379 ping` 均返回 `PONG`。

## B1 变更纪律

- 本次 B0 只新增验证文档，不修改 RC2 代码。
- `v0.9.0-RC2` tag 未移动，Part C 未开始。
- 后续 RC2 期间只接受 blocker/high 缺陷和明确兼容性修复；任何代码修复必须独立提交并重新执行 A3，下一版本使用 RC3，不得移动 RC2 tag。
