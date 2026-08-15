# 持续集成与生产方言矩阵

`.github/workflows/ci.yml` 在 Pull Request 和 `main` 推送时执行：

- JDK 17、21 全仓 `clean verify`。
- 自动配置副作用、版本、测试命名、模块成熟度和 Maven 依赖收敛门禁。
- Redis 7.4 下的 Sa-Token 会话、幂等原子占位和验证码一次性消费测试。
- Testcontainers MySQL 8.4、PostgreSQL 16 的 Tenant + DataPermission + Pagination 方言矩阵。
- 仓库外 Maven 项目导入 BOM 并消费 Web、Encrypt Starter 的冒烟测试。

方言矩阵覆盖别名、JOIN、EXISTS 子查询、分页总数和用户自定义 `MybatisPlusInterceptor` 自动合并路径。

本地 Docker 可用时执行：

```bash
YCR_DIALECT_INTEGRATION_TESTS=true \
YCR_TEST_DATABASE=mysql \
mvn -pl :ycr-starter-data-permission -am \
  -Dtest=SqlDialectMatrixIntegrationTest,CustomInterceptorSqlDialectIntegrationTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dapi.version=1.40 test

YCR_DIALECT_INTEGRATION_TESTS=true \
YCR_TEST_DATABASE=postgresql \
mvn -pl :ycr-starter-data-permission -am \
  -Dtest=SqlDialectMatrixIntegrationTest,CustomInterceptorSqlDialectIntegrationTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dapi.version=1.40 test
```

未设置 `YCR_DIALECT_INTEGRATION_TESTS=true` 时，日常本地单元测试不会启动容器。
`api.version=1.40` 用于兼容 Docker Desktop 29+ 的最低 Docker API 要求，同时兼容 GitHub Actions 运行环境。
