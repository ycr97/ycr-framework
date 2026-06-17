package com.ycr.framework.log.aop;

import cn.hutool.core.util.StrUtil;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.log.annotation.Log;
import com.ycr.framework.log.autoconfigure.LogProperties;
import com.ycr.framework.log.enums.Include;
import com.ycr.framework.log.handler.LogHandler;
import com.ycr.framework.log.model.LogRecord;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 操作日志切面
 *
 * <p>环绕 {@code @Log} 标注的方法：在方法执行前同步采集方法/请求/操作人信息（保证异步落库不丢上下文），
 * 对敏感参数脱敏后，执行业务并记录状态/耗时，最终交给 {@link LogHandler} 处理（可同步或异步）。</p>
 *
 * @author ycr
 */
@Slf4j
@Aspect
public class LogAspect {

    /** 脱敏占位 */
    private static final String MASK = "******";

    private final LogHandler logHandler;
    private final LogProperties properties;
    /** 异步执行器，可为 null：null 时退化为同步落库 */
    private final Executor executor;
    /** 预先小写化的敏感键，匹配时不区分大小写 */
    private final Set<String> sensitiveKeys;

    public LogAspect(LogHandler logHandler, LogProperties properties, Executor executor) {
        this.logHandler = logHandler;
        this.properties = properties;
        this.executor = executor;
        this.sensitiveKeys = properties.getSensitiveKeys().stream()
                .map(k -> k.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    @Around("@annotation(com.ycr.framework.log.annotation.Log) || @within(com.ycr.framework.log.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Log logAnnotation = resolveLogAnnotation(joinPoint);
        // 注解标记忽略：直接放行，不记录
        if (logAnnotation.ignore()) {
            return joinPoint.proceed();
        }

        LogRecord record = new LogRecord();
        record.setOperateTime(LocalDateTime.now());
        long startTime = System.currentTimeMillis();

        // 执行前同步采集，确保异步落库时上下文不丢
        Set<Include> includes = resolveIncludes(logAnnotation);
        fillMethodInfo(joinPoint, logAnnotation, record);
        fillRequestInfo(record, includes);
        fillOperatorInfo(record);

        try {
            Object result = joinPoint.proceed();
            record.setStatus(200);
            return result;
        } catch (Throwable e) {
            record.setStatus(500);
            record.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            record.setElapsedTime(System.currentTimeMillis() - startTime);
            dispatch(record);
        }
    }

    private Log resolveLogAnnotation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log methodLog = method.getAnnotation(Log.class);
        if (methodLog != null) {
            return methodLog;
        }
        return joinPoint.getTarget().getClass().getAnnotation(Log.class);
    }

    /** 同步/异步分发，异步执行器缺失时退化为同步 */
    private void dispatch(LogRecord record) {
        if (properties.isAsync() && executor != null) {
            executor.execute(() -> safeHandle(record));
        } else {
            safeHandle(record);
        }
    }

    /** 兜住处理器异常，避免影响业务主流程 */
    private void safeHandle(LogRecord record) {
        try {
            logHandler.handle(record);
        } catch (Exception e) {
            log.error("操作日志记录失败", e);
        }
    }

    private void fillMethodInfo(JoinPoint joinPoint, Log logAnnotation, LogRecord record) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        record.setClassName(joinPoint.getTarget().getClass().getName());
        record.setMethodName(method.getName());
        record.setDescription(StrUtil.isNotBlank(logAnnotation.value()) ? logAnnotation.value() : method.getName());

        // 模块：方法级优先，空则回退类级 @Log
        String module = logAnnotation.module();
        if (StrUtil.isBlank(module)) {
            Log classLog = joinPoint.getTarget().getClass().getAnnotation(Log.class);
            if (classLog != null && StrUtil.isNotBlank(classLog.module())) {
                module = classLog.module();
            }
        }
        record.setModule(module);
    }

    private void fillRequestInfo(LogRecord record, Set<Include> includes) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        record.setRequestMethod(request.getMethod());
        record.setRequestUrl(request.getRequestURI());

        if (includes.contains(Include.IP_ADDRESS)) {
            record.setClientIp(getClientIp(request));
        }
        if (includes.contains(Include.REQUEST_PARAMS)) {
            record.setRequestParams(maskAndFormat(request.getParameterMap()));
        }
    }

    private void fillOperatorInfo(LogRecord record) {
        if (UserContextHolder.get() != null) {
            record.setOperatorId(UserContextHolder.getUserId());
            record.setOperatorName(UserContextHolder.getUsername());
        }
    }

    /** 全局采集项叠加注解 includes/excludes */
    private Set<Include> resolveIncludes(Log logAnnotation) {
        Set<Include> includes = EnumSet.noneOf(Include.class);
        includes.addAll(properties.getIncludes());
        includes.addAll(Arrays.asList(logAnnotation.includes()));
        Arrays.asList(logAnnotation.excludes()).forEach(includes::remove);
        return includes;
    }

    /** 拼接参数为 k=v&...，敏感键脱敏 */
    private String maskAndFormat(Map<String, String[]> parameterMap) {
        StringJoiner joiner = new StringJoiner("&");
        parameterMap.forEach((key, values) -> {
            String value = isSensitive(key) ? MASK : String.join(",", values);
            joiner.add(key + "=" + value);
        });
        return joiner.toString();
    }

    private boolean isSensitive(String key) {
        return key != null && sensitiveKeys.contains(key.toLowerCase(Locale.ROOT));
    }

    /** 解析客户端真实 IP：X-Forwarded-For → X-Real-IP → RemoteAddr */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
