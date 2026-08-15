package com.ycr.framework.trace.generator;

import java.util.UUID;

/**
 * 默认 TraceId 生成器 —— UUID 去横线
 *
 * @author ycr
 */
public class UuidTraceIdGenerator implements TraceIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
