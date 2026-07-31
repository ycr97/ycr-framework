package com.ycr.framework.ddd.extension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ExtensionExecutor 路由执行测试
 *
 * @author ycr
 */
class ExtensionExecutorTest {

    interface GreetExtPt extends ExtensionPointI {
        String greet(String name);
    }

    private ExtensionExecutor executorWith(GreetExtPt ext, BizScenario at) {
        ExtensionRepository repo = new ExtensionRepository();
        if (ext != null) {
            repo.register(GreetExtPt.class, at, ext);
        }
        return new ExtensionExecutor(repo);
    }

    @Test
    @DisplayName("execute命中并执行")
    void shouldMatchExpectedBehavior001() {
        BizScenario sc = BizScenario.of("b");
        ExtensionExecutor executor = executorWith(name -> "hi:" + name, sc);

        String r = executor.execute(GreetExtPt.class, sc, ext -> ext.greet("Tom"));
        assertEquals("hi:Tom", r);
    }

    @Test
    @DisplayName("execute未命中抛异常")
    void shouldMatchExpectedBehavior002() {
        ExtensionExecutor executor = executorWith(null, null);
        assertThrows(IllegalStateException.class,
                () -> executor.execute(GreetExtPt.class, BizScenario.of("b"), ext -> ext.greet("x")));
    }

    @Test
    @DisplayName("executeWithDefault未命中返默认")
    void shouldMatchExpectedBehavior003() {
        ExtensionExecutor executor = executorWith(null, null);
        String r = executor.executeWithDefault(GreetExtPt.class, BizScenario.of("b"),
                ext -> ext.greet("x"), "默认");
        assertEquals("默认", r);
    }
}
