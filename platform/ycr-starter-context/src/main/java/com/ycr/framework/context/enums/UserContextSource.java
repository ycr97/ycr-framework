package com.ycr.framework.context.enums;

/**
 * 用户上下文来源。
 *
 * @author ycr
 */
public enum UserContextSource {

    /** 由网关或可信上游透传的签名上下文还原 */
    GATEWAY_HEADER,

    /** 由 token 解析还原 */
    TOKEN,

    /** 由业务代码手工设置 */
    MANUAL,

    /** 系统任务、定时任务或内部任务上下文 */
    SYSTEM
}
