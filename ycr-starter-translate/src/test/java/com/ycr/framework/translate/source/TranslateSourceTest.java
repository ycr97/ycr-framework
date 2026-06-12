package com.ycr.framework.translate.source;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 翻译源 + 注册表行为测试
 *
 * @author ycr
 */
class TranslateSourceTest {

    private final EnumTranslateSource enumSource = new EnumTranslateSource();

    @Test
    void 枚举源以类名加编码翻译为描述_容忍字符串数字差异() {
        String key = StatusEnum.class.getName();
        assertEquals("启用", enumSource.translate(key, 1));
        assertEquals("启用", enumSource.translate(key, "1"));
        assertEquals("禁用", enumSource.translate(key, 0));
    }

    @Test
    void 枚举源非法输入返回null() {
        String key = StatusEnum.class.getName();
        assertNull(enumSource.translate(key, 99));
        assertNull(enumSource.translate(key, null));
        assertNull(enumSource.translate("", 1));
        assertNull(enumSource.translate("com.not.Exist", 1));
    }

    @Test
    void 字典源委托DictProvider() {
        DictProvider provider = (dictCode, itemCode) ->
                "user_status".equals(dictCode) && "1".equals(itemCode) ? "启用" : null;
        DictTranslateSource dictSource = new DictTranslateSource(provider);

        assertEquals("dict", dictSource.name());
        assertEquals("启用", dictSource.translate("user_status", 1));
        assertNull(dictSource.translate("user_status", 9));
        assertNull(dictSource.translate("", 1));
    }

    @Test
    void 注册表按名索引_未注册返回null_同名覆盖() {
        TranslateSource custom = new TranslateSource() {
            @Override
            public String name() {
                return "enum";
            }

            @Override
            public String translate(String key, Object code) {
                return "覆盖";
            }
        };
        // 后注册的 custom 覆盖内置 enum 源
        TranslateSourceRegistry registry = new TranslateSourceRegistry(List.of(enumSource, custom));

        assertSame(custom, registry.get("enum"));
        assertNull(registry.get("dict"));
        assertNull(registry.get(null));
    }
}
