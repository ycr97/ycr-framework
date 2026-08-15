package com.ycr.framework.ddd.statemachine;

/**
 * 不可变的状态转换定义：source --[event] (when condition) --> target，附带 action。
 * <p>构建期一次成型，运行期只读，可在多线程间安全复用。
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
public final class Transition<S, E, C> {

    private final S source;
    private final S target;
    private final E event;
    /** 转换条件，null 表示无条件（作为该 (source,event) 的兜底分支） */
    private final Condition<C> condition;
    /** 转换动作，null 表示无副作用 */
    private final Action<S, E, C> action;

    public Transition(S source, S target, E event, Condition<C> condition, Action<S, E, C> action) {
        this.source = source;
        this.target = target;
        this.event = event;
        this.condition = condition;
        this.action = action;
    }

    public S getSource() {
        return source;
    }

    public S getTarget() {
        return target;
    }

    public E getEvent() {
        return event;
    }

    public Condition<C> getCondition() {
        return condition;
    }

    public Action<S, E, C> getAction() {
        return action;
    }

    /** 是否为无条件兜底转换 */
    public boolean isUnconditional() {
        return condition == null;
    }
}
