package com.ycr.framework.log.aspect;

import cn.hutool.core.util.StrUtil;
import com.ycr.framework.log.annotation.MethodLog;
import com.ycr.framework.log.autoconfigure.LogProperties;
import com.ycr.framework.log.enums.Level;
import com.ycr.framework.log.util.LogJsonSupport;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 方法调用日志切面（开发排障型）
 *
 * <p>环绕 {@code @MethodLog}，把入参/出参/耗时/异常打到 SLF4J。双控：装配开关 + 日志级别门控
 * （配置级别未开启则连序列化都跳过，生产默认零开销）。序列化经 {@link LogJsonSupport}，与审计日志彻底分离。</p>
 *
 * @author ycr
 */
@Slf4j
@Aspect
public class MethodLogAspect {

    private final LogJsonSupport jsonSupport;
    private final LogProperties.Method config;

    public MethodLogAspect(LogJsonSupport jsonSupport, LogProperties.Method config) {
        this.jsonSupport = jsonSupport;
        this.config = config;
    }

    @Around("@annotation(com.ycr.framework.log.annotation.MethodLog) "
            + "|| @within(com.ycr.framework.log.annotation.MethodLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 级别门控：未开启直接放行，连序列化都不做
        if (!levelEnabled()) {
            return joinPoint.proceed();
        }
        MethodLog annotation = resolve(joinPoint);
        String tag = tag(joinPoint, annotation);

        if (annotation.args()) {
            emit("{} 入参: {}", tag, serializeArgs(joinPoint));
        }
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            if (annotation.result()) {
                emit("{} 出参: {} | 耗时: {}ms", tag,
                        jsonSupport.serialize(result, config.getMaxLength()),
                        System.currentTimeMillis() - start);
            }
            return result;
        } catch (Throwable e) {
            emit("{} 异常: {} | 耗时: {}ms", tag, e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }

    private boolean levelEnabled() {
        return config.getLevel() == Level.INFO ? log.isInfoEnabled() : log.isDebugEnabled();
    }

    private void emit(String pattern, Object... args) {
        if (config.getLevel() == Level.INFO) {
            log.info(pattern, args);
        } else {
            log.debug(pattern, args);
        }
    }

    private MethodLog resolve(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        MethodLog methodAnn = method.getAnnotation(MethodLog.class);
        if (methodAnn != null) {
            return methodAnn;
        }
        return joinPoint.getTarget().getClass().getAnnotation(MethodLog.class);
    }

    private String tag(JoinPoint joinPoint, MethodLog annotation) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String desc = StrUtil.isNotBlank(annotation.value()) ? annotation.value() : method.getName();
        return "[方法日志] " + desc + " " + joinPoint.getTarget().getClass().getSimpleName()
                + "#" + method.getName();
    }

    /** 过滤噪声类型后序列化入参列表。 */
    private String serializeArgs(JoinPoint joinPoint) {
        List<Object> kept = Arrays.stream(joinPoint.getArgs())
                .filter(a -> !jsonSupport.isSkippable(a))
                .toList();
        return jsonSupport.serialize(kept, config.getMaxLength());
    }
}
