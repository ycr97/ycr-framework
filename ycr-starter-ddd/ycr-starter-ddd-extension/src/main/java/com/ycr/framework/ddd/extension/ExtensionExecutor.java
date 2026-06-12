package com.ycr.framework.ddd.extension;

import java.util.function.Function;

/**
 * 扩展点执行器 —— 按业务身份路由到扩展实现并执行
 *
 * @author ycr
 */
public class ExtensionExecutor {

    private final ExtensionRepository repository;

    public ExtensionExecutor(ExtensionRepository repository) {
        this.repository = repository;
    }

    /**
     * 执行扩展点；未找到实现抛 {@link IllegalStateException}
     */
    public <Ext extends ExtensionPointI, R> R execute(Class<Ext> extPtClass, BizScenario scenario,
                                                      Function<Ext, R> function) {
        Ext ext = repository.getExt(extPtClass, scenario);
        if (ext == null) {
            throw new IllegalStateException("未找到扩展点实现: " + extPtClass.getName()
                    + " for " + scenario.getUniqueIdentity());
        }
        return function.apply(ext);
    }

    /**
     * 执行扩展点；未找到实现返回 {@code defaultValue}
     */
    public <Ext extends ExtensionPointI, R> R executeWithDefault(Class<Ext> extPtClass, BizScenario scenario,
                                                                 Function<Ext, R> function, R defaultValue) {
        Ext ext = repository.getExt(extPtClass, scenario);
        if (ext == null) {
            return defaultValue;
        }
        return function.apply(ext);
    }
}
