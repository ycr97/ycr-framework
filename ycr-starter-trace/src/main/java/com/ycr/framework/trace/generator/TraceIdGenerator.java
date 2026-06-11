package com.ycr.framework.trace.generator;

/**
 * TraceId 生成器
 *
 * <p>业务方可实现此接口自定义 TraceId 格式（如雪花、带机器标识前缀等），
 * 框架通过 {@code @ConditionalOnMissingBean} 让自定义实现覆盖默认 {@link UuidTraceIdGenerator}。</p>
 *
 * @author ycr
 */
public interface TraceIdGenerator {

    /**
     * 生成一个 TraceId
     */
    String generate();
}
