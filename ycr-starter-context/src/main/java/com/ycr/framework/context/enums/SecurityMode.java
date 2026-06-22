package com.ycr.framework.context.enums;

/**
 * 同步请求上下文安全模式。
 *
 * @author ycr
 */
public enum SecurityMode {

    /** 只信任带签名且未过期的上游上下文头 */
    GATEWAY_TRUST,

    /** 忽略身份头，只通过 token resolver 还原上下文 */
    TOKEN_VERIFY,

    /** 优先签名上下文头，缺失时 fallback token，身份冲突时拒绝 */
    MIXED
}
