# 仓库结构与模块边界

YCR Framework 采用“一级架构分区、分区内模块平铺”的仓库结构：

```text
ycr-framework/
├── build/       # Parent、第三方依赖版本、YCR BOM
├── foundation/  # 最基础的共享能力
├── platform/    # 应用 Runtime Spine
├── extensions/  # 按需启用的稳定基础设施能力
└── incubator/   # Experimental maturity 隔离区
```

物理目录只用于组织源码，不进入 Maven namespace，也不改变 `groupId`、`artifactId`、Java package、配置前缀或运行时行为。

## 架构职责

| 区域 | 模块 |
| --- | --- |
| Build | `ycr-dependencies`、`ycr-framework-bom` |
| Foundation | `ycr-starter-core`、`ycr-common`、`ycr-starter-json`、`ycr-starter-validation` |
| Platform | `ycr-starter-context`、`ycr-starter-web`、`ycr-starter-data-*`、`ycr-starter-security` |
| Extensions | Auth、Cache、Tenant、Data Permission、Feign、MQ、Storage、Trace 等可选能力 |
| Incubator | `ycr-starter-business`、`ycr-starter-crud`、`ycr-starter-ddd-*`、`ycr-starter-sdk` |

Foundation 是当前共享基础层，不等价于纯 Java kernel；Core 轻量化拆分是独立的 1.0 架构任务。

## 依赖方向

```text
Foundation ← Platform ← Extensions
```

- Foundation 不得生产依赖 Platform、Extensions、Incubator。
- Platform 允许依赖 Foundation，不得生产依赖 Extensions、Incubator。
- Extensions 允许依赖 Foundation、Platform，并允许必要的 Extension 间依赖。
- Incubator 可按需依赖 Foundation、Platform、Extensions。
- Stable 模块不得生产依赖 Incubator。
- `test` scope 依赖不构成 Runtime 边界违规。

边界由 `scripts/check-module-boundaries.sh` 在 CI 中持续验证。

## 模块选择

局部 Maven reactor 构建使用 artifactId selector，避免命令绑定物理路径：

```bash
mvn -pl :ycr-starter-data-permission -am test
```

功能分类保留在 README 和模块文档中，不创建 `extensions/data`、`extensions/security` 等二级目录。
