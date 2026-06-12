package com.ycr.framework.translate.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TranslateType 测试：仅保留有真实落点的三种类型，内置类型有固定源名
 *
 * @author ycr
 */
class TranslateTypeTest {

    @Test
    void 仅含三种类型() {
        assertEquals(3, TranslateType.values().length);
    }

    @Test
    void 内置类型有固定源名() {
        assertEquals("dict", TranslateType.DICT.getSourceName());
        assertEquals("enum", TranslateType.ENUM.getSourceName());
    }

    @Test
    void CUSTOM无固定源名() {
        assertNull(TranslateType.CUSTOM.getSourceName());
    }
}
