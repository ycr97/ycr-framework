package com.ycr.framework.log.handler;

import com.ycr.framework.log.model.LogRecord;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认操作日志处理器 —— 打印到日志
 *
 * <p>仅作开箱即用的兜底；生产环境应由业务方实现 {@link LogHandler} 落库覆盖。</p>
 *
 * @author ycr
 */
@Slf4j
public class Slf4jLogHandler implements LogHandler {

    @Override
    public void handle(LogRecord logRecord) {
        log.info("event=operation_log traceId={} tenantId={} clientId={} module={} operation={} "
                        + "method={} uri={} status={} elapsedMs={} operatorId={} operatorName={}",
                logRecord.getTraceId(),
                logRecord.getTenantId(),
                logRecord.getClientId(),
                logRecord.getModule(),
                logRecord.getDescription(),
                logRecord.getRequestMethod(),
                logRecord.getRequestUrl(),
                logRecord.getStatus(),
                logRecord.getElapsedTime(),
                logRecord.getOperatorId(),
                logRecord.getOperatorName());
    }
}
