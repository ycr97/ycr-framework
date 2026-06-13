package com.ycr.framework.ddd.statemachine;

import com.ycr.framework.core.exception.SysException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 状态机全局注册表：可选地按 id 注册状态机，供运行期跨组件按 id 查找。
 * <p>注册与构建解耦——{@link StateMachine} 由 Builder 构造后是否进入全局表由调用方决定。
 * 同 id 重复注册：同实例幂等放过，异实例 fail-fast（暴露"两台机器争同一 id"的配置错误）。
 */
public final class StateMachineFactory {

    private static final String CONFIG_ERROR = "STATE_MACHINE_CONFIG_ERROR";

    private static final Map<String, StateMachine<?, ?, ?>> MACHINES = new ConcurrentHashMap<>();

    private StateMachineFactory() {
    }

    /**
     * 注册状态机。同 id 已存在且为不同实例时抛 {@link SysException}。
     */
    public static void register(StateMachine<?, ?, ?> stateMachine) {
        StateMachine<?, ?, ?> existing = MACHINES.putIfAbsent(stateMachine.getMachineId(), stateMachine);
        if (existing != null && existing != stateMachine) {
            throw new SysException(CONFIG_ERROR,
                    "状态机 id 冲突: " + stateMachine.getMachineId() + " 已被另一实例注册");
        }
    }

    /**
     * 按 id 获取状态机，不存在返回 null。
     *
     * @param <S> 状态类型
     * @param <E> 事件类型
     * @param <C> 上下文类型
     */
    @SuppressWarnings("unchecked")
    public static <S, E, C> StateMachine<S, E, C> get(String machineId) {
        return (StateMachine<S, E, C>) MACHINES.get(machineId);
    }
}
