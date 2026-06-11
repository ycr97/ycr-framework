package com.ycr.framework.log.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志记录
 *
 * <p>由 {@code LogAspect} 在方法执行前后填充，最终交给 {@code LogHandler} 落库。
 * 操作人信息取自 {@code UserContextHolder}，与 auth/context 闭环复用。</p>
 *
 * @author ycr
 */
@Data
public class LogRecord implements Serializable {

    /** 日志描述 */
    private String description;

    /** 所属模块 */
    private String module;

    /** 请求方法（GET/POST…） */
    private String requestMethod;

    /** 请求 URL */
    private String requestUrl;

    /** 请求头（按 includes 采集，已脱敏） */
    private Map<String, String> requestHeaders;

    /** 请求参数（按 includes 采集，已脱敏） */
    private String requestParams;

    /** 请求体（按 includes 采集，已脱敏） */
    private String requestBody;

    /** 响应状态码：成功 200，异常 500 */
    private Integer status;

    /** 响应体（按 includes 采集） */
    private String responseBody;

    /** 错误信息（异常时填充） */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long elapsedTime;

    /** 客户端 IP */
    private String clientIp;

    /** IP 归属地（预留，按需由处理器解析） */
    private String ipLocation;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 操作人 ID */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 操作时间 */
    private LocalDateTime operateTime;

    /** 调用的类名 */
    private String className;

    /** 调用的方法名 */
    private String methodName;
}
