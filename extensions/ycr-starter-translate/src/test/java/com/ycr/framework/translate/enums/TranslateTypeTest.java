package com.ycr.framework.translate.enums;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("仅含三种类型")
    void shouldMatchExpectedBehavior001() {
        assertEquals(3, TranslateType.values().length);
    }

    @Test
    @DisplayName("内置类型有固定源名")
    void shouldMatchExpectedBehavior002() {
        assertEquals("dict", TranslateType.DICT.getSourceName());
        assertEquals("enum", TranslateType.ENUM.getSourceName());
    }

    @Test
    @DisplayName("CUSTOM无固定源名")
    void shouldMatchExpectedBehavior003() {
        assertNull(TranslateType.CUSTOM.getSourceName());
    }
}
