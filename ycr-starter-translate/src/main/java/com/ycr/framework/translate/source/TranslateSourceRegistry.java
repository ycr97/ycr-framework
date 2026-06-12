package com.ycr.framework.translate.source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 翻译源注册表
 *
 * <p>启动时聚合容器内所有 {@link TranslateSource}，按 {@link TranslateSource#name()} 建索引，
 * 供 Jackson 序列化器按名取源。同名后注册者覆盖前者（允许应用覆盖内置源）。</p>
 *
 * @author ycr
 */
public class TranslateSourceRegistry {

    private final Map<String, TranslateSource> sources = new HashMap<>();

    public TranslateSourceRegistry(List<TranslateSource> sourceList) {
        if (sourceList != null) {
            for (TranslateSource source : sourceList) {
                sources.put(source.name(), source);
            }
        }
    }

    /**
     * 按源名取翻译源
     *
     * @param name 源名（{@code dict} / {@code enum} / 自定义）
     * @return 翻译源；未注册返回 {@code null}
     */
    public TranslateSource get(String name) {
        return name == null ? null : sources.get(name);
    }
}
