package com.ycr.framework.log.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志记录
 *
 * <p>由 {@code LogAspect} 在方法执行前后填充，最终交给 {@code LogHandler} 落库。
 * 操作人信息取自 {@code UserContextHolder}，与 auth/context 闭环复用。</p>
 *
 * <p>仅保留框架当前会真实填充的字段；请求头/请求体/响应体/浏览器/归属地等采集项尚未实现
 * （前提见 {@code Include} 的说明），故不在模型中预留空字段。</p>
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

    /** 请求参数（按 includes 采集，敏感键已脱敏） */
    private String requestParams;

    /** 响应状态码：成功 200，异常 500 */
    private Integer status;

    /** 错误信息（异常时填充） */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long elapsedTime;

    /** 客户端 IP（按 includes 采集） */
    private String clientIp;

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
