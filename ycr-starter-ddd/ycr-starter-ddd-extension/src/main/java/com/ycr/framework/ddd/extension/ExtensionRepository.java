package com.ycr.framework.ddd.extension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展点仓库 —— 存储与降级查找扩展实现
 *
 * @author ycr
 */
public class ExtensionRepository {

    private final Map<String, ExtensionPointI> extensionMap = new ConcurrentHashMap<>();

    /**
     * 注册扩展实现
     *
     * <p>同键已存在<b>不同实例</b>视为配置冲突，fail-fast 抛异常；同实例重复注册
     * （{@code ContextRefreshedEvent} 可能多次触发）幂等放过。</p>
     */
    public <Ext extends ExtensionPointI> void register(Class<Ext> extPtClass, BizScenario scenario, Ext extension) {
        String key = buildKey(extPtClass, scenario);
        ExtensionPointI existing = extensionMap.get(key);
        if (existing != null && existing != extension) {
            throw new IllegalStateException("扩展点冲突，同一身份存在多个实现: " + key
                    + "（已有 " + existing.getClass().getName() + "，新 " + extension.getClass().getName() + "）");
        }
        extensionMap.put(key, extension);
    }

    /**
     * 降级查找：精确 → useCase 级 → bizId 级 → 全局默认
     *
     * @return 命中的扩展实现；全部未命中返回 {@code null}
     */
    @SuppressWarnings("unchecked")
    public <Ext extends ExtensionPointI> Ext getExt(Class<Ext> extPtClass, BizScenario scenario) {
        Ext ext = (Ext) extensionMap.get(buildKey(extPtClass, scenario));
        if (ext != null) {
            return ext;
        }
        ext = (Ext) extensionMap.get(buildKey(extPtClass,
                BizScenario.of(scenario.getBizId(), scenario.getUseCase())));
        if (ext != null) {
            return ext;
        }
        ext = (Ext) extensionMap.get(buildKey(extPtClass, BizScenario.of(scenario.getBizId())));
        if (ext != null) {
            return ext;
        }
        return (Ext) extensionMap.get(buildKey(extPtClass, BizScenario.of(BizScenario.DEFAULT_BIZ_ID)));
    }

    private String buildKey(Class<?> extPtClass, BizScenario scenario) {
        return extPtClass.getName() + "@" + scenario.getUniqueIdentity();
    }
}
