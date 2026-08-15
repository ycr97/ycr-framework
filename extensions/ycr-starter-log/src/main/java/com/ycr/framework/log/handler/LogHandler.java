package com.ycr.framework.log.handler;

import com.ycr.framework.log.model.LogRecord;

/**
 * 操作日志处理器 SPI
 *
 * <p>业务方实现此接口即可自定义落库方式（DB / MQ / ES 等）。框架通过 {@code @ConditionalOnMissingBean}
 * 让业务实现自动覆盖默认的 {@link Slf4jLogHandler}。实现方应自行兜住异常，切面也会再兜一层。</p>
 *
 * @author ycr
 */
public interface LogHandler {

    /**
     * 处理一条操作日志记录
     *
     * @param logRecord 已采集填充的日志记录
     */
    void handle(LogRecord logRecord);
}
