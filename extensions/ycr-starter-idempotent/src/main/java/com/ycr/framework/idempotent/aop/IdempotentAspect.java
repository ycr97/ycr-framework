package com.ycr.framework.idempotent.aop;

import cn.hutool.core.text.CharSequenceUtil;
import com.ycr.framework.idempotent.annotation.Idempotent;
import com.ycr.framework.idempotent.autoconfigure.IdempotentProperties;
import com.ycr.framework.idempotent.exception.IdempotentException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 幂等切面 —— 基于 Redisson SETNX（{@code RBucket.trySet}）
 *
 * <p>环绕 {@code @Idempotent} 方法：解析幂等键（前缀 + 名称 + 可选 SpEL 键），在时间窗口内 {@code trySet}
 * 占位成功才放行，重复请求抛 {@link IdempotentException}；业务执行抛异常时删除占位键以允许重试。</p>
 *
 * @author ycr
 */
@Slf4j
@Aspect
public class IdempotentAspect {

    private final IdempotentProperties properties;
    private final RedissonClient redissonClient;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public IdempotentAspect(IdempotentProperties properties, RedissonClient redissonClient) {
        this.properties = properties;
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String key = resolveKey(joinPoint, idempotent);
        RBucket<String> bucket = redissonClient.getBucket(key);
        String ownerToken = UUID.randomUUID().toString();

        // SETNX：占位成功才放行，失败即为重复提交
        if (!bucket.trySet(ownerToken, idempotent.timeout(), idempotent.unit())) {
            throw new IdempotentException(idempotent.message());
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // 业务异常释放占位键，避免把失败请求误锁在窗口期内
            bucket.compareAndSet(ownerToken, null);
            throw e;
        }
    }

    /** 解析幂等键：前缀 : 名称 [: SpEL 后缀] */
    private String resolveKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        if (idempotent.timeout() <= 0) {
            throw new IllegalStateException("@Idempotent.timeout 必须大于 0");
        }
        if (CharSequenceUtil.isBlank(idempotent.key())) {
            throw new IllegalStateException("@Idempotent.key 必须显式配置，避免同一方法的不同请求共用全局键");
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String name = CharSequenceUtil.isNotBlank(idempotent.name())
                ? idempotent.name()
                : method.getDeclaringClass().getName() + "#" + method.getName();

        StringBuilder keyBuilder = new StringBuilder(properties.getKeyPrefix()).append(":").append(name);

        String spelValue = evaluateSpel(idempotent.key(), joinPoint, method);
        if (CharSequenceUtil.isBlank(spelValue)) {
            throw new IllegalStateException("@Idempotent.key 求值结果不能为空");
        }
        keyBuilder.append(":").append(spelValue);
        return keyBuilder.toString();
    }

    /** 按方法入参对 SpEL 表达式求值 */
    private String evaluateSpel(String expression, ProceedingJoinPoint joinPoint, Method method) {
        EvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, joinPoint.getArgs(), parameterNameDiscoverer);
        Object value = parser.parseExpression(expression).getValue(context);
        return value == null ? "" : value.toString();
    }
}
