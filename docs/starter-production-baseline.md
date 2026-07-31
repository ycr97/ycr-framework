# Starter 生产级装配基线

## 默认策略

- 无副作用能力可默认开启。
- 修改 SQL、请求响应、认证授权、消息消费、外部中间件状态的能力默认关闭。
- 引入 starter 不等于启用能力。

当前须显式开启的能力包括数据权限、字段加密、幂等、XSS、限流、鉴权切面、租户、RocketMQ、文件存储、CORS 和统一响应包装。完整清单见 [自动配置矩阵](starter-autoconfiguration-matrix.md)。

## 自动装配测试矩阵

1. 默认配置
2. `enabled=false`
3. `enabled=true` + 完整配置
4. `enabled=true` + 缺少关键配置
5. 用户自定义 Bean 覆盖

新增或修改 AutoConfiguration 时，必须同步更新矩阵并通过：

```bash
scripts/check-autoconfiguration-tests.sh
```

## 迁移

升级后需显式配置需要的能力：

```yaml
ycr:
  security:
    enabled: true
  tenant:
    enabled: true
  encrypt:
    enabled: true
  protect:
    xss:
      enabled: true
  storage:
    enabled: true
  web:
    cors:
      enabled: true
    response:
      enabled: true
```

只开启应用实际使用且已完成依赖、密钥、规则和异常场景验证的能力。
