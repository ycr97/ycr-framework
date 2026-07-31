package com.ycr.framework.ddd.extension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * BizScenario 身份 + ExtensionRepository 降级查找与冲突测试
 *
 * @author ycr
 */
class ExtensionRepositoryTest {

    interface PriceExtPt extends ExtensionPointI {
        int calc();
    }

    @Test
    @DisplayName("BizScenario三级身份与默认值")
    void shouldMatchExpectedBehavior001() {
        assertEquals("bizA#uc1#sc1", BizScenario.of("bizA", "uc1", "sc1").getUniqueIdentity());
        assertEquals("bizA#" + BizScenario.DEFAULT_USE_CASE + "#" + BizScenario.DEFAULT_SCENARIO,
                BizScenario.of("bizA").getUniqueIdentity());
        assertEquals("bizA#uc1#" + BizScenario.DEFAULT_SCENARIO,
                BizScenario.of("bizA", "uc1").getUniqueIdentity());
    }

    @Test
    @DisplayName("精确命中")
    void shouldMatchExpectedBehavior002() {
        ExtensionRepository repo = new ExtensionRepository();
        PriceExtPt ext = () -> 1;
        repo.register(PriceExtPt.class, BizScenario.of("b", "u", "s"), ext);

        assertSame(ext, repo.getExt(PriceExtPt.class, BizScenario.of("b", "u", "s")));
    }

    @Test
    @DisplayName("降级_注册bizId级_查全场景命中")
    void shouldMatchExpectedBehavior003() {
        ExtensionRepository repo = new ExtensionRepository();
        PriceExtPt bizLevel = () -> 2;
        repo.register(PriceExtPt.class, BizScenario.of("b"), bizLevel);

        // 全场景精确无 -> 退 useCase 级无 -> 退 bizId 级命中
        assertSame(bizLevel, repo.getExt(PriceExtPt.class, BizScenario.of("b", "u", "s")));
    }

    @Test
    @DisplayName("降级_全局默认兜底")
    void shouldMatchExpectedBehavior004() {
        ExtensionRepository repo = new ExtensionRepository();
        PriceExtPt def = () -> 9;
        repo.register(PriceExtPt.class, BizScenario.of(BizScenario.DEFAULT_BIZ_ID), def);

        assertSame(def, repo.getExt(PriceExtPt.class, BizScenario.of("unknownBiz", "u", "s")));
    }

    @Test
    @DisplayName("全部未命中返null")
    void shouldMatchExpectedBehavior005() {
        ExtensionRepository repo = new ExtensionRepository();
        assertNull(repo.getExt(PriceExtPt.class, BizScenario.of("x")));
    }

    @Test
    @DisplayName("同键异实例冲突抛异常_同实例幂等")
    void shouldMatchExpectedBehavior006() {
        ExtensionRepository repo = new ExtensionRepository();
        PriceExtPt a = () -> 1;
        PriceExtPt b = () -> 2;
        BizScenario sc = BizScenario.of("b", "u", "s");

        repo.register(PriceExtPt.class, sc, a);
        repo.register(PriceExtPt.class, sc, a); // 同实例幂等

        assertThrows(IllegalStateException.class, () -> repo.register(PriceExtPt.class, sc, b));
    }
}
