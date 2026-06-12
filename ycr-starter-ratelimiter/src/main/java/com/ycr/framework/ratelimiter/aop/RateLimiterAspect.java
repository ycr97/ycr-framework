package com.ycr.framework.ratelimiter.aop;

import cn.hutool.core.text.CharSequenceUtil;
import com.ycr.framework.ratelimiter.annotation.RateLimiter;
import com.ycr.framework.ratelimiter.autoconfigure.RateLimiterProperties;
import com.ycr.framework.ratelimiter.enums.LimitType;
import com.ycr.framework.ratelimiter.exception.RateLimiterException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面
 *
 * <p>环绕 {@code @RateLimiter} 方法：解析限流键（前缀 + 名称 + 维度后缀 + 可选 SpEL 键），
 * 取 Redisson 令牌桶（首次 {@code trySetRate}），{@code tryAcquire} 失败抛 {@link RateLimiterException}。</p>
 *
 * @author ycr
 */
@Slf4j
@Aspect
public class RateLimiterAspect {

    private final RateLimiterProperties properties;
    private final RedissonClient redissonClient;
    /** 实例级缓存：已创建并配置过速率的令牌桶，避免每次请求重复 setRate */
    private final ConcurrentHashMap<String, RRateLimiter> limiterCache = new ConcurrentHashMap<>();
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public RateLimiterAspect(RateLimiterProperties properties, RedissonClient redissonClient) {
        this.properties = properties;
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        if (isRateLimited(joinPoint, rateLimiter)) {
            throw new RateLimiterException(rateLimiter.message());
        }
        return joinPoint.proceed();
    }

    private boolean isRateLimited(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        try {
            String key = resolveKey(joinPoint, rateLimiter);
            RateType rateType = rateLimiter.type() == LimitType.CLUSTER ? RateType.PER_CLIENT : RateType.OVERALL;
            Duration interval = Duration.ofMillis(rateLimiter.unit().toMillis(rateLimiter.interval()));

            RRateLimiter limiter = limiterCache.computeIfAbsent(key, k -> {
                RRateLimiter l = redissonClient.getRateLimiter(k);
                // 首次创建时设定速率；已存在则 trySetRate 返回 false，速率以首次为准
                l.trySetRate(rateType, rateLimiter.rate(), interval);
                return l;
            });

            return !limiter.tryAcquire();
        } catch (RateLimiterException e) {
            throw e;
        } catch (Exception e) {
            throw new RateLimiterException("服务器限流异常，请稍候再试", e);
        }
    }

    /** 解析限流键：前缀 : 名称 [: 维度后缀] [: SpEL 后缀] */
    private String resolveKey(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String name = CharSequenceUtil.isNotBlank(rateLimiter.name())
                ? rateLimiter.name()
                : method.getDeclaringClass().getName() + "#" + method.getName();

        StringBuilder keyBuilder = new StringBuilder(properties.getKeyPrefix()).append(":").append(name);

        String dimensionSuffix = resolveDimensionSuffix(rateLimiter);
        if (CharSequenceUtil.isNotBlank(dimensionSuffix)) {
            keyBuilder.append(":").append(dimensionSuffix);
        }

        if (CharSequenceUtil.isNotBlank(rateLimiter.key())) {
            String spelValue = evaluateSpel(rateLimiter.key(), joinPoint, method);
            if (CharSequenceUtil.isNotBlank(spelValue)) {
                keyBuilder.append(":").append(spelValue);
            }
        }
        return keyBuilder.toString();
    }

    /** IP / 集群维度后缀 */
    private String resolveDimensionSuffix(RateLimiter rateLimiter) {
        if (rateLimiter.type() == LimitType.IP) {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRemoteAddr();
            }
            return "";
        }
        if (rateLimiter.type() == LimitType.CLUSTER) {
            return redissonClient.getId();
        }
        return "";
    }

    /** 按方法入参对 SpEL 表达式求值 */
    private String evaluateSpel(String expression, ProceedingJoinPoint joinPoint, Method method) {
        EvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, joinPoint.getArgs(), parameterNameDiscoverer);
        Object value = parser.parseExpression(expression).getValue(context);
        return value == null ? "" : value.toString();
    }
}
