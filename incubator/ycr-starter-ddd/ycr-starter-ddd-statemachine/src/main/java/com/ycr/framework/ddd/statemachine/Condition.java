package com.ycr.framework.ddd.statemachine;

/**
 * 状态转换条件：判断当前上下文是否允许该转换发生。
 * <p>同一 (源状态, 事件) 可定义多条带不同条件的转换以实现条件分支路由。
 *
 * @param <C> 上下文类型
 */
@FunctionalInterface
public interface Condition<C> {

    /**
     * @param context 业务上下文
     * @return true 表示满足、允许流转
     */
    boolean isSatisfied(C context);
}
