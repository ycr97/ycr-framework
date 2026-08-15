package com.ycr.framework.business.chain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 业务拦截链编排与回退语义测试
 *
 * @author ycr
 */
class BizInterceptorChainTest {

    /** 记录型拦截器：把各阶段写入共享 trace */
    static class Recorder implements BizInterceptor {
        final String tag;
        final int order;
        final List<String> trace;
        final boolean vetoOnBefore;

        Recorder(String tag, int order, List<String> trace, boolean vetoOnBefore) {
            this.tag = tag;
            this.order = order;
            this.trace = trace;
            this.vetoOnBefore = vetoOnBefore;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public void before(BizContext c) {
            trace.add("before-" + tag);
            if (vetoOnBefore) {
                throw new IllegalStateException("veto-" + tag);
            }
        }

        @Override
        public void after(BizContext c) {
            trace.add("after-" + tag);
        }

        @Override
        public void onError(BizContext c, Throwable e) {
            trace.add("onError-" + tag);
        }
    }

    private BizContext ctx() {
        return new BizContext(null, null, new Object[0], null);
    }

    @Test
    @DisplayName("正常_before正序_after逆序_回填result")
    void shouldMatchExpectedBehavior001() throws Throwable {
        List<String> trace = new ArrayList<>();
        // 故意乱序传入，验证按 order 排序：a(order1) 先于 b(order2)
        BizInterceptorChain chain = new BizInterceptorChain(List.of(
                new Recorder("b", 2, trace, false),
                new Recorder("a", 1, trace, false)));
        BizContext ctx = ctx();

        Object result = chain.execute(ctx, () -> {
            trace.add("action");
            return "OK";
        });

        assertEquals("OK", result);
        assertEquals("OK", ctx.getResult());
        assertEquals(List.of("before-a", "before-b", "action", "after-b", "after-a"), trace);
    }

    @Test
    @DisplayName("before否决_目标不执行_仅已成功者逆序onError")
    void shouldMatchExpectedBehavior002() {
        List<String> trace = new ArrayList<>();
        BizInterceptorChain chain = new BizInterceptorChain(List.of(
                new Recorder("a", 1, trace, false),
                new Recorder("b", 2, trace, true),   // b 否决
                new Recorder("c", 3, trace, false)));
        BizContext ctx = ctx();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> chain.execute(ctx, () -> {
                    trace.add("action");
                    return "OK";
                }));

        assertEquals("veto-b", ex.getMessage());
        // c 的 before 从未执行；action 未执行；onError 仅对 a（b 自身 before 抛出不计入已成功）
        assertEquals(List.of("before-a", "before-b", "onError-a"), trace);
        assertFalse(trace.contains("action"));
        assertEquals(ex, ctx.getError());
    }

    @Test
    @DisplayName("目标动作抛出_全部逆序onError_after不调")
    void shouldMatchExpectedBehavior003() {
        List<String> trace = new ArrayList<>();
        BizInterceptorChain chain = new BizInterceptorChain(List.of(
                new Recorder("a", 1, trace, false),
                new Recorder("b", 2, trace, false)));
        BizContext ctx = ctx();

        RuntimeException boom = new RuntimeException("boom");
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> chain.execute(ctx, () -> {
                    throw boom;
                }));

        assertEquals(boom, thrown);
        assertEquals(List.of("before-a", "before-b", "onError-b", "onError-a"), trace);
        assertFalse(trace.contains("after-a"));
    }

    @Test
    @DisplayName("attributes跨拦截器共享")
    void shouldMatchExpectedBehavior004() throws Throwable {
        List<String> seen = new ArrayList<>();
        BizInterceptor writer = new BizInterceptor() {
            @Override
            public int getOrder() {
                return 1;
            }

            @Override
            public void before(BizContext c) {
                c.setAttribute("k", "v");
            }
        };
        BizInterceptor reader = new BizInterceptor() {
            @Override
            public int getOrder() {
                return 2;
            }

            @Override
            public void before(BizContext c) {
                seen.add(String.valueOf(c.getAttribute("k")));
            }
        };
        BizContext ctx = ctx();
        new BizInterceptorChain(List.of(reader, writer)).execute(ctx, () -> null);

        assertEquals(List.of("v"), seen);
    }
}
