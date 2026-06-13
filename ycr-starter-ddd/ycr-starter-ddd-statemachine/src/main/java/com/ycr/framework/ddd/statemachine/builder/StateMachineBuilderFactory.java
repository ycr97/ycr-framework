package com.ycr.framework.ddd.statemachine.builder;

/**
 * 状态机构建器工厂：提供带类型推断的 {@link StateMachineBuilder} 入口。
 */
public final class StateMachineBuilderFactory {

    private StateMachineBuilderFactory() {
    }

    /**
     * 创建一个新的状态机构建器。
     *
     * @param <S> 状态类型
     * @param <E> 事件类型
     * @param <C> 上下文类型
     */
    public static <S, E, C> StateMachineBuilder<S, E, C> create() {
        return new StateMachineBuilder<>();
    }
}
