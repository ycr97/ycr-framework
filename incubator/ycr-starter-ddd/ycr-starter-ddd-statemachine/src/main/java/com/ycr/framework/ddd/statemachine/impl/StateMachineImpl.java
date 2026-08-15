package com.ycr.framework.ddd.statemachine.impl;

import com.ycr.framework.core.exception.BizException;
import com.ycr.framework.ddd.statemachine.State;
import com.ycr.framework.ddd.statemachine.StateMachine;
import com.ycr.framework.ddd.statemachine.Transition;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 状态机实现：构建期由 Builder 注入完整的 stateMap，之后只读、线程安全。
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
@Slf4j
public class StateMachineImpl<S, E, C> implements StateMachine<S, E, C> {

    /** 状态流转拒绝的错误码（HTTP 400，属业务规则拒绝） */
    private static final String TRANSITION_REJECTED = "STATE_MACHINE_TRANSITION_REJECTED";

    private final String machineId;
    private final Map<S, State<S, E, C>> stateMap;

    /**
     * @param machineId 状态机标识
     * @param stateMap  构建期已成型的状态表（实现不再修改，调用方需保证不外泄可变引用）
     */
    public StateMachineImpl(String machineId, Map<S, State<S, E, C>> stateMap) {
        this.machineId = machineId;
        this.stateMap = stateMap;
    }

    @Override
    public S fireEvent(S sourceState, E event, C context) {
        State<S, E, C> state = stateMap.get(sourceState);
        if (state == null) {
            throw new BizException(TRANSITION_REJECTED,
                    "状态机[" + machineId + "]未定义状态: " + sourceState);
        }
        if (!state.hasEvent(event)) {
            throw new BizException(TRANSITION_REJECTED,
                    "状态机[" + machineId + "]状态 " + sourceState + " 上没有事件 " + event + " 的转换定义");
        }
        Transition<S, E, C> transition = state.select(event, context);
        if (transition == null) {
            throw new BizException(TRANSITION_REJECTED,
                    "状态机[" + machineId + "]状态 " + sourceState + " 上事件 " + event + " 的转换条件均不满足");
        }
        S target = transition.getTarget();
        if (transition.getAction() != null) {
            transition.getAction().execute(sourceState, target, event, context);
        }
        if (log.isDebugEnabled()) {
            log.debug("状态机[{}] 状态转换: {} --[{}]--> {}", machineId, sourceState, event, target);
        }
        return target;
    }

    @Override
    public boolean verify(S sourceState, E event) {
        State<S, E, C> state = stateMap.get(sourceState);
        return state != null && state.hasEvent(event);
    }

    @Override
    public String getMachineId() {
        return machineId;
    }
}
