package com.ycr.framework.business.chain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 业务拦截链
 *
 * <p>单点编排一次 {@code @BizApi} 调用的全生命周期，保证回退语义正确（对标 Spring HandlerInterceptor）：</p>
 * <ol>
 *     <li>按 order 升序执行各拦截器 {@code before}，记录已成功者；</li>
 *     <li>某 {@code before} 抛出 → <b>不</b>执行目标动作，仅对「已成功的 before」逆序 {@code onError}，原异常上抛；</li>
 *     <li>全部 {@code before} 通过 → 执行目标动作：成功则逆序 {@code after} 并回填 result；动作抛出则逆序 {@code onError} 并上抛（after 不调）。</li>
 * </ol>
 *
 * @author ycr
 */
public class BizInterceptorChain {

    /**
     * 目标动作（通常为 {@code joinPoint::proceed}）
     */
    @FunctionalInterface
    public interface BizAction {
        Object execute() throws Throwable;
    }

    private final List<BizInterceptor> interceptors;

    public BizInterceptorChain(List<BizInterceptor> interceptors) {
        List<BizInterceptor> sorted = new ArrayList<>(interceptors == null ? List.of() : interceptors);
        sorted.sort(Comparator.comparingInt(BizInterceptor::getOrder));
        this.interceptors = List.copyOf(sorted);
    }

    /**
     * 执行拦截链并环绕目标动作
     *
     * @param context 本次调用上下文
     * @param action  目标动作
     * @return 目标动作返回值
     * @throws Throwable before 否决或目标动作抛出的原始异常
     */
    public Object execute(BizContext context, BizAction action) throws Throwable {
        // 记录已成功执行 before 的拦截器（用于精确回退）
        int passed = 0;
        try {
            for (; passed < interceptors.size(); passed++) {
                interceptors.get(passed).before(context);
            }
        } catch (Throwable beforeError) {
            context.setError(beforeError);
            // 仅对「已成功的 before」逆序回调 onError；passed 当前指向抛出者，从 passed-1 起
            unwindOnError(context, passed - 1, beforeError);
            throw beforeError;
        }

        Object result;
        try {
            result = action.execute();
        } catch (Throwable actionError) {
            context.setError(actionError);
            unwindOnError(context, interceptors.size() - 1, actionError);
            throw actionError;
        }

        context.setResult(result);
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).after(context);
        }
        return result;
    }

    private void unwindOnError(BizContext context, int fromIndex, Throwable error) {
        for (int i = fromIndex; i >= 0; i--) {
            interceptors.get(i).onError(context, error);
        }
    }
}
