package com.ycr.framework.ddd.extension;

/**
 * 业务场景身份 —— bizId + useCase + scenario 三级组合
 *
 * <p>用于扩展点路由：精确身份找不到实现时按 useCase 级、bizId 级、全局默认逐级降级。</p>
 *
 * @author ycr
 */
public class BizScenario {

    public static final String DEFAULT_BIZ_ID = "#defaultBizId#";
    public static final String DEFAULT_USE_CASE = "#defaultUseCase#";
    public static final String DEFAULT_SCENARIO = "#defaultScenario#";

    private final String bizId;
    private final String useCase;
    private final String scenario;

    private BizScenario(String bizId, String useCase, String scenario) {
        this.bizId = bizId;
        this.useCase = useCase;
        this.scenario = scenario;
    }

    public static BizScenario of(String bizId) {
        return new BizScenario(bizId, DEFAULT_USE_CASE, DEFAULT_SCENARIO);
    }

    public static BizScenario of(String bizId, String useCase) {
        return new BizScenario(bizId, useCase, DEFAULT_SCENARIO);
    }

    public static BizScenario of(String bizId, String useCase, String scenario) {
        return new BizScenario(bizId, useCase, scenario);
    }

    public String getBizId() {
        return bizId;
    }

    public String getUseCase() {
        return useCase;
    }

    public String getScenario() {
        return scenario;
    }

    /**
     * 唯一身份串 {@code bizId#useCase#scenario}
     */
    public String getUniqueIdentity() {
        return bizId + "#" + useCase + "#" + scenario;
    }
}
