package com.ycr.framework.context.propagation;

/**
 * 可扩展的线程上下文访问器，用于将模块自有的线程状态纳入统一传播和清理边界。
 *
 * @author ycr
 */
public interface ThreadContextAccessor {

    /** 捕获当前线程状态；返回值仅会交还给同一访问器。 */
    Object capture();

    /** 恢复状态；传入 {@code null} 时必须清理当前线程状态。 */
    void restore(Object captured);
}
