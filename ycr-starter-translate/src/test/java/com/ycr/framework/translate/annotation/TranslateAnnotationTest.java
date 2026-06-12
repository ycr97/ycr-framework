package com.ycr.framework.translate.annotation;

import com.ycr.framework.translate.enums.TranslateType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Translate 注解契约测试
 *
 * @author ycr
 */
class TranslateAnnotationTest {

    static class Sample {
        @Translate(type = TranslateType.DICT, key = "user_status")
        String status;

        @Translate(type = TranslateType.ENUM, targetField = "genderText")
        Integer gender;

        String noAnno;
    }

    @Test
    void 注解默认值应符合约定() throws Exception {
        Field field = Sample.class.getDeclaredField("status");
        Translate t = field.getAnnotation(Translate.class);
        assertEquals(TranslateType.DICT, t.type());
        assertEquals("user_status", t.key());
        assertEquals("", t.source());
        assertEquals("", t.targetField());
    }

    @Test
    void 可显式指定目标字段() throws Exception {
        Field field = Sample.class.getDeclaredField("gender");
        Translate t = field.getAnnotation(Translate.class);
        assertEquals(TranslateType.ENUM, t.type());
        assertEquals("genderText", t.targetField());
    }

    @Test
    void 未标注字段无注解() throws Exception {
        Field field = Sample.class.getDeclaredField("noAnno");
        assertTrue(field.getAnnotation(Translate.class) == null);
    }
}
