package com.ycr.framework.ddd.statemachine.builder;

import com.ycr.framework.ddd.statemachine.Action;
import com.ycr.framework.ddd.statemachine.Condition;
import com.ycr.framework.ddd.statemachine.State;
import com.ycr.framework.ddd.statemachine.StateMachine;
import com.ycr.framework.ddd.statemachine.Transition;
import com.ycr.framework.ddd.statemachine.impl.StateMachineImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 状态机构建器：流式 DSL 声明转换，{@link #build} 一次成型为不可变状态机。
 * <pre>
 * builder.externalTransition()
 *        .from(INIT).to(PAID).on(PAY)
 *        .when(ctx -&gt; ctx.paid()).perform(action);
 * StateMachine&lt;S,E,C&gt; sm = builder.build("orderSM");
 * </pre>
 * <p>构建器非线程安全、一次性使用；产物 {@link StateMachine} 不可变、可并发复用。
 * build 不会自动注册到 {@link com.ycr.framework.ddd.statemachine.StateMachineFactory}，
 * 需要全局按 id 查找时请显式 register。
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
public class StateMachineBuilder<S, E, C> {

    private final List<TransitionDef<S, E, C>> defs = new ArrayList<>();

    /**
     * 开启一条外部转换（源状态 != 目标状态）的声明。
     */
    public ExternalTransitionBuilder<S, E, C> externalTransition() {
        return new ExternalTransitionBuilder<>(this);
    }

    void addDef(TransitionDef<S, E, C> def) {
        defs.add(def);
    }

    /**
     * 构造不可变状态机。会校验同 (源,事件) 不存在多条无条件转换（否则抛 SysException）。
     *
     * @param machineId 状态机标识
     */
    public StateMachine<S, E, C> build(String machineId) {
        Map<S, State<S, E, C>> stateMap = new HashMap<>();
        for (TransitionDef<S, E, C> def : defs) {
            Transition<S, E, C> transition =
                    new Transition<>(def.from, def.to, def.event, def.condition, def.action);
            stateMap.computeIfAbsent(def.from, State::new).addTransition(transition);
            // 确保目标状态在表中存在（即便其上暂无出向转换），便于诊断
            stateMap.computeIfAbsent(def.to, State::new);
        }
        return new StateMachineImpl<>(machineId, stateMap);
    }

    /** 单条转换的暂存定义 */
    static final class TransitionDef<S, E, C> {
        S from;
        S to;
        E event;
        Condition<C> condition;
        Action<S, E, C> action;
    }

    /**
     * 外部转换流式构建器：from → to → on → (when) → perform。
     * {@code perform} 为终结操作，调用后转换被登记到父构建器。
     */
    public static class ExternalTransitionBuilder<S, E, C> {

        private final StateMachineBuilder<S, E, C> parent;
        private final TransitionDef<S, E, C> def = new TransitionDef<>();

        ExternalTransitionBuilder(StateMachineBuilder<S, E, C> parent) {
            this.parent = parent;
        }

        public ExternalTransitionBuilder<S, E, C> from(S state) {
            def.from = state;
            return this;
        }

        public ExternalTransitionBuilder<S, E, C> to(S state) {
            def.to = state;
            return this;
        }

        public ExternalTransitionBuilder<S, E, C> on(E event) {
            def.event = event;
            return this;
        }

        /** 可选条件；省略则该转换为无条件兜底。 */
        public ExternalTransitionBuilder<S, E, C> when(Condition<C> condition) {
            def.condition = condition;
            return this;
        }

        /** 终结：登记本条转换。action 可传 (from,to,event,ctx) -&gt; {} 表示无副作用。 */
        public void perform(Action<S, E, C> action) {
            def.action = action;
            parent.addDef(def);
        }
    }
}
