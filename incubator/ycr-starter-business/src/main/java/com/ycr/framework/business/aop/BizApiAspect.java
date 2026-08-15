package com.ycr.framework.business.aop;

import com.ycr.framework.business.annotation.BizApi;
import com.ycr.framework.business.chain.BizContext;
import com.ycr.framework.business.chain.BizInterceptorChain;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 业务接入点切面
 *
 * <p>环绕标注 {@link BizApi} 的方法：由 joinPoint 构造 {@link BizContext}，交由 {@link BizInterceptorChain}
 * 环绕真实方法执行（{@code joinPoint::proceed} 作为目标动作），由链统一管控前置/后置/异常与回退。</p>
 *
 * @author ycr
 */
@Aspect
public class BizApiAspect {

    private final BizInterceptorChain chain;

    public BizApiAspect(BizInterceptorChain chain) {
        this.chain = chain;
    }

    @Around("@annotation(bizApi)")
    public Object around(ProceedingJoinPoint joinPoint, BizApi bizApi) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        BizContext context = new BizContext(method, joinPoint.getTarget(), joinPoint.getArgs(), bizApi);
        // 以 context 的当前入参执行，从而支持 before 阶段改写参数后真正生效
        return chain.execute(context, () -> joinPoint.proceed(context.getArgs()));
    }
}
