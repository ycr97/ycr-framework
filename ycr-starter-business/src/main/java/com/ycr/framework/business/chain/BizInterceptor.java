package com.ycr.framework.business.chain;

/**
 * 业务拦截器 SPI
 *
 * <p>实现本接口并注册为 Spring Bean，即可插入 {@code @BizApi} 拦截链。按 {@link #getOrder()} 升序执行
 * {@link #before}，逆序执行 {@link #after}/{@link #onError}（仅对 before 已成功者回调，见 {@code BizInterceptorChain}）。</p>
 *
 * @author ycr
 */
public interface BizInterceptor {

    /**
     * 执行顺序，值越小越先 before（after/onError 则越后）
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 前置处理；抛出异常即否决本次调用（目标方法不执行）
     */
    default void before(BizContext context) {
    }

    /**
     * 后置处理（目标方法成功返回后，逆序回调），可读 {@link BizContext#getResult()}
     */
    default void after(BizContext context) {
    }

    /**
     * 异常处理（before 否决或目标方法抛出时，逆序回调），可读 {@link BizContext#getError()}
     */
    default void onError(BizContext context, Throwable error) {
    }
}
