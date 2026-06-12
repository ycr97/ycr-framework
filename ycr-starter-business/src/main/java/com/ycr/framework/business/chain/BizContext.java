package com.ycr.framework.business.chain;

import com.ycr.framework.business.annotation.BizApi;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务接入点上下文
 *
 * <p>贯穿一次 {@code @BizApi} 调用的全生命周期，承载方法元数据、参数、注解、可变共享属性，
 * 以及执行后的返回值/异常，供各 {@code BizInterceptor} 读写与相互传递信息。</p>
 *
 * @author ycr
 */
public class BizContext {

    private final Method method;
    private final Object target;
    private final Object[] args;
    private final BizApi bizApi;
    /** 跨拦截器共享的可变属性袋 */
    private final Map<String, Object> attributes = new HashMap<>();

    /** 目标方法返回值（成功后由链回填，供 after 阶段读取） */
    private Object result;
    /** 目标方法抛出的异常（异常时由链回填，供 onError 阶段读取） */
    private Throwable error;

    public BizContext(Method method, Object target, Object[] args, BizApi bizApi) {
        this.method = method;
        this.target = target;
        this.args = args;
        this.bizApi = bizApi;
    }

    /**
     * 接入点名称：取 {@link BizApi#value()}，为空时回退方法名
     */
    public String getName() {
        if (bizApi != null && !bizApi.value().isEmpty()) {
            return bizApi.value();
        }
        return method == null ? "" : method.getName();
    }

    public Method getMethod() {
        return method;
    }

    public Object getTarget() {
        return target;
    }

    public Object[] getArgs() {
        return args;
    }

    public BizApi getBizApi() {
        return bizApi;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
