package com.ycr.framework.ddd.statemachine;

/**
 * 状态机接口：声明式描述合法状态流转，运行期由 {@link #fireEvent} 驱动。
 * <p>实现为不可变、线程安全，可作为单例复用。
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
public interface StateMachine<S, E, C> {

    /**
     * 触发事件，执行匹配转换的动作并返回目标状态。
     *
     * @param sourceState 当前状态
     * @param event       触发事件
     * @param context     业务上下文
     * @return 目标状态
     * @throws com.ycr.framework.core.exception.BizException 源状态未定义、无对应事件转换、或条件均不满足
     */
    S fireEvent(S sourceState, E event, C context);

    /**
     * 校验 (源状态, 事件) 是否存在转换定义（不计算条件）。
     */
    boolean verify(S sourceState, E event);

    /**
     * 状态机标识。
     */
    String getMachineId();
}
