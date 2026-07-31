package com.ycr.framework.ddd.statemachine;

import com.ycr.framework.core.exception.BizException;
import com.ycr.framework.core.exception.SysException;
import com.ycr.framework.ddd.statemachine.builder.StateMachineBuilder;
import com.ycr.framework.ddd.statemachine.builder.StateMachineBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateMachineTest {

    enum OrderState { INIT, PAID, SHIPPED, COMPLETED }

    enum OrderEvent { PAY, SHIP, COMPLETE }

    static class OrderContext {
        String orderId;
        int score;
        String trace = "";

        OrderContext(String orderId) {
            this.orderId = orderId;
        }
    }

    private StateMachine<OrderState, OrderEvent, OrderContext> linearMachine(String id) {
        StateMachineBuilder<OrderState, OrderEvent, OrderContext> builder = StateMachineBuilderFactory.create();
        builder.externalTransition().from(OrderState.INIT).to(OrderState.PAID).on(OrderEvent.PAY)
                .when(ctx -> true).perform((from, to, event, ctx) -> {});
        builder.externalTransition().from(OrderState.PAID).to(OrderState.SHIPPED).on(OrderEvent.SHIP)
                .when(ctx -> true).perform((from, to, event, ctx) -> {});
        return builder.build(id);
    }

    @Test
    @DisplayName("线性流转应按定义推进状态")
    void shouldMatchExpectedBehavior001() {
        StateMachine<OrderState, OrderEvent, OrderContext> sm = linearMachine("linear");
        OrderContext ctx = new OrderContext("order001");

        assertEquals(OrderState.PAID, sm.fireEvent(OrderState.INIT, OrderEvent.PAY, ctx));
        assertEquals(OrderState.SHIPPED, sm.fireEvent(OrderState.PAID, OrderEvent.SHIP, ctx));
    }

    @Test
    @DisplayName("状态上无该事件的转换应抛BizException")
    void shouldMatchExpectedBehavior002() {
        StateMachine<OrderState, OrderEvent, OrderContext> sm = linearMachine("noTransition");
        assertThrows(BizException.class,
                () -> sm.fireEvent(OrderState.INIT, OrderEvent.SHIP, new OrderContext("order002")));
    }

    @Test
    @DisplayName("源状态未定义应抛BizException")
    void shouldMatchExpectedBehavior003() {
        StateMachine<OrderState, OrderEvent, OrderContext> sm = linearMachine("noState");
        assertThrows(BizException.class,
                () -> sm.fireEvent(OrderState.COMPLETED, OrderEvent.PAY, new OrderContext("order003")));
    }

    @Test
    @DisplayName("条件分支应按condition路由不同目标")
    void shouldMatchExpectedBehavior004() {
        // REVIEW --SUBMIT--> APPROVED (score>=60) / 否则兜底 REJECTED
        StateMachineBuilder<ReviewState, ReviewEvent, OrderContext> builder = StateMachineBuilderFactory.create();
        builder.externalTransition().from(ReviewState.REVIEW).to(ReviewState.APPROVED).on(ReviewEvent.SUBMIT)
                .when(ctx -> ctx.score >= 60).perform((from, to, event, ctx) -> {});
        builder.externalTransition().from(ReviewState.REVIEW).to(ReviewState.REJECTED).on(ReviewEvent.SUBMIT)
                .perform((from, to, event, ctx) -> {}); // 无 when = 兜底
        StateMachine<ReviewState, ReviewEvent, OrderContext> sm = builder.build("review");

        OrderContext pass = new OrderContext("p");
        pass.score = 80;
        assertEquals(ReviewState.APPROVED, sm.fireEvent(ReviewState.REVIEW, ReviewEvent.SUBMIT, pass));

        OrderContext fail = new OrderContext("f");
        fail.score = 50;
        assertEquals(ReviewState.REJECTED, sm.fireEvent(ReviewState.REVIEW, ReviewEvent.SUBMIT, fail));
    }

    @Test
    @DisplayName("唯一转换条件不满足应抛BizException")
    void shouldMatchExpectedBehavior005() {
        StateMachineBuilder<OrderState, OrderEvent, OrderContext> builder = StateMachineBuilderFactory.create();
        builder.externalTransition().from(OrderState.INIT).to(OrderState.PAID).on(OrderEvent.PAY)
                .when(ctx -> false).perform((from, to, event, ctx) -> {});
        StateMachine<OrderState, OrderEvent, OrderContext> sm = builder.build("condFail");

        assertThrows(BizException.class,
                () -> sm.fireEvent(OrderState.INIT, OrderEvent.PAY, new OrderContext("order004")));
    }

    @Test
    @DisplayName("action应在流转时真实执行并收到正确入参")
    void shouldMatchExpectedBehavior006() {
        AtomicReference<String> seen = new AtomicReference<>();
        StateMachineBuilder<OrderState, OrderEvent, OrderContext> builder = StateMachineBuilderFactory.create();
        builder.externalTransition().from(OrderState.INIT).to(OrderState.PAID).on(OrderEvent.PAY)
                .perform((from, to, event, ctx) -> {
                    ctx.trace = from + ">" + to + ":" + event;
                    seen.set(ctx.orderId);
                });
        StateMachine<OrderState, OrderEvent, OrderContext> sm = builder.build("action");

        OrderContext ctx = new OrderContext("order005");
        OrderState target = sm.fireEvent(OrderState.INIT, OrderEvent.PAY, ctx);

        assertEquals(OrderState.PAID, target);
        assertEquals("INIT>PAID:PAY", ctx.trace);
        assertEquals("order005", seen.get());
    }

    @Test
    @DisplayName("verify应反映转换是否定义")
    void shouldMatchExpectedBehavior007() {
        StateMachine<OrderState, OrderEvent, OrderContext> sm = linearMachine("verify");
        assertTrue(sm.verify(OrderState.INIT, OrderEvent.PAY));
        assertFalse(sm.verify(OrderState.INIT, OrderEvent.SHIP));
        assertFalse(sm.verify(OrderState.COMPLETED, OrderEvent.PAY));
    }

    @Test
    @DisplayName("Factory应支持注册与按id查找")
    void shouldMatchExpectedBehavior008() {
        StateMachine<OrderState, OrderEvent, OrderContext> sm = linearMachine("factoryReg");
        StateMachineFactory.register(sm);
        assertSame(sm, StateMachineFactory.get("factoryReg"));
        assertNull(StateMachineFactory.get("不存在的机器"));
    }

    @Test
    @DisplayName("Factory重复注册同id异实例应failfast同实例幂等")
    void shouldMatchExpectedBehavior009() {
        StateMachine<OrderState, OrderEvent, OrderContext> a = linearMachine("dupId");
        StateMachine<OrderState, OrderEvent, OrderContext> b = linearMachine("dupId");
        StateMachineFactory.register(a);
        StateMachineFactory.register(a); // 同实例幂等，不抛
        assertThrows(SysException.class, () -> StateMachineFactory.register(b)); // 异实例冲突
    }

    @Test
    @DisplayName("同源同事件出现两条无条件转换build应抛SysException")
    void shouldMatchExpectedBehavior010() {
        StateMachineBuilder<OrderState, OrderEvent, OrderContext> builder = StateMachineBuilderFactory.create();
        builder.externalTransition().from(OrderState.INIT).to(OrderState.PAID).on(OrderEvent.PAY)
                .perform((from, to, event, ctx) -> {});
        builder.externalTransition().from(OrderState.INIT).to(OrderState.SHIPPED).on(OrderEvent.PAY)
                .perform((from, to, event, ctx) -> {});
        assertThrows(SysException.class, () -> builder.build("dupUnconditional"));
    }

    enum ReviewState { REVIEW, APPROVED, REJECTED }

    enum ReviewEvent { SUBMIT }
}
