# DDD 扩展点

`ycr-starter-ddd-extension` 提供 COLA 风格的扩展点机制：同一业务能力按「业务身份 + 用例 + 场景」路由到不同实现，解决多租户/多渠道/多业态的分支膨胀。

## 依赖

```xml
<dependency>
    <groupId>com.ycr.framework</groupId>
    <artifactId>ycr-starter-ddd-extension</artifactId>
</dependency>
```

无配置项。`ExtensionBootstrap` 在容器刷新时扫描 `@Extension` Bean 注册到 `ExtensionRepository`（用 `ultimateTargetClass` 取真实类，兼容被 AOP 代理的扩展）。

## 1. 定义扩展点接口

继承标记接口 `ExtensionPointI`：

```java
public interface PriceCalculateExtPt extends ExtensionPointI {
    BigDecimal calculate(Order order);
}
```

## 2. 提供多个实现

用 `@Extension` 声明各实现适用的业务场景（`bizId` / `useCase` / `scenario` 三级，默认值代表通配）：

```java
@Extension(bizId = "retail")
@Component
public class RetailPriceExt implements PriceCalculateExtPt {
    public BigDecimal calculate(Order order) { /* 零售算价 */ }
}

@Extension(bizId = "wholesale")
@Component
public class WholesalePriceExt implements PriceCalculateExtPt {
    public BigDecimal calculate(Order order) { /* 批发算价 */ }
}
```

## 3. 路由执行

注入 `ExtensionExecutor`，按 `BizScenario` 选中实现：

```java
@RequiredArgsConstructor
@Service
public class PricingService {
    private final ExtensionExecutor executor;

    public BigDecimal price(Order order, String bizId) {
        BizScenario scenario = BizScenario.of(bizId);          // of(bizId) / of(bizId, useCase) / of(bizId, useCase, scenario)
        return executor.execute(PriceCalculateExtPt.class, scenario, ext -> ext.calculate(order));
    }
}
```

| 方法 | 说明 |
| --- | --- |
| `execute(extPtClass, scenario, callback)` | 路由到匹配扩展执行；无匹配抛异常 |
| `executeWithDefault(extPtClass, scenario, callback)` | 无匹配时回退到默认扩展 |

`BizScenario.getUniqueIdentity()` 为三段拼接的唯一键，匹配按精确 → 默认逐级回退。
