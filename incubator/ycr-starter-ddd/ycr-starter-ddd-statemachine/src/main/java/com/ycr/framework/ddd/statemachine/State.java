package com.ycr.framework.ddd.statemachine;

import com.ycr.framework.core.exception.SysException;

import java.util.ArrayList;
import java.util.List;

/**
 * 状态节点：持有以该状态为源的所有转换，并负责按事件 + 条件选择目标转换。
 * <p>转换在构建期通过 {@link #addTransition} 加入，构建完成后只读。
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
public class State<S, E, C> {

    /** 状态机配置错误的错误码（HTTP 500） */
    private static final String CONFIG_ERROR = "STATE_MACHINE_CONFIG_ERROR";

    private final S stateId;
    private final List<Transition<S, E, C>> transitions = new ArrayList<>();

    public State(S stateId) {
        this.stateId = stateId;
    }

    public S getStateId() {
        return stateId;
    }

    /**
     * 构建期加入一条转换。防呆：同一事件至多允许一条无条件（兜底）转换，
     * 否则后者将静默遮蔽前者，属配置错误，fail-fast。
     */
    public void addTransition(Transition<S, E, C> transition) {
        if (transition.isUnconditional()) {
            for (Transition<S, E, C> existing : transitions) {
                if (existing.getEvent().equals(transition.getEvent()) && existing.isUnconditional()) {
                    throw new SysException(CONFIG_ERROR, "状态 " + stateId + " 上事件 " + transition.getEvent()
                            + " 存在多条无条件转换，无法确定兜底目标");
                }
            }
        }
        transitions.add(transition);
    }

    /** 该状态上是否定义了针对该事件的任何转换（不考虑条件），供 verify 使用 */
    public boolean hasEvent(E event) {
        for (Transition<S, E, C> t : transitions) {
            if (t.getEvent().equals(event)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 选择目标转换：先按声明顺序取第一条「带条件且条件满足」的；
     * 全不满足时回退到无条件兜底转换；都没有则返回 null。
     *
     * @return 命中的转换，或 null（无满足条件的转换）
     */
    public Transition<S, E, C> select(E event, C context) {
        Transition<S, E, C> fallback = null;
        for (Transition<S, E, C> t : transitions) {
            if (!t.getEvent().equals(event)) {
                continue;
            }
            if (t.isUnconditional()) {
                fallback = t;
            } else if (t.getCondition().isSatisfied(context)) {
                return t;
            }
        }
        return fallback;
    }
}
