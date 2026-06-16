package com.ycr.framework.cache.jetcache.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * JetCache 声明式缓存配置。
 *
 * <p>仅约束「方法缓存切面」的装配行为；缓存后端（远程 Redisson / 本地 Caffeine）的细节
 * 走 JetCache 原生 {@code jetcache.*} 配置，由
 * {@link JetCacheDefaultsEnvironmentPostProcessor} 提供可被覆盖的默认值。</p>
 *
 * @author ycr
 */
@ConfigurationProperties(prefix = "ycr.cache.jetcache")
public class JetCacheAnnoProperties {

    /** 是否启用声明式缓存，默认启用。 */
    private boolean enabled = true;

    /**
     * 方法缓存扫描的基础包。
     *
     * <p>默认 {@code [""]} 表示全量扫描（{@code CachePointcut} 内部以 {@code startsWith} 匹配，
     * 且固定剔除 {@code java*} / {@code org.springframework*} / CGLIB 增强类）。
     * 生产环境建议显式收窄为业务根包（如 {@code com.acme}）以降低首次调用的注解解析开销。</p>
     */
    private String[] basePackages = {""};

    /**
     * 缓存增强器的执行顺序，默认最低优先级。
     *
     * <p>保证缓存切面位于事务等切面「之内」：先进事务、再查缓存，命中缓存时仍处于事务边界内。</p>
     */
    private int order = Ordered.LOWEST_PRECEDENCE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String[] getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(String[] basePackages) {
        this.basePackages = basePackages;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
