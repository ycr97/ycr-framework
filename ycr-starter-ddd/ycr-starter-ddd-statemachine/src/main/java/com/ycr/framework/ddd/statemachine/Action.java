package com.ycr.framework.ddd.statemachine;

/**
 * 状态转换动作：在一次合法流转发生时执行的副作用（如发领域事件、改聚合字段）。
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
@FunctionalInterface
public interface Action<S, E, C> {

    /**
     * @param from    源状态
     * @param to      目标状态
     * @param event   触发事件
     * @param context 业务上下文
     */
    void execute(S from, S to, E event, C context);
}
