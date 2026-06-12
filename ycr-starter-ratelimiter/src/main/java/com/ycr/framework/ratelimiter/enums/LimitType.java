package com.ycr.framework.ratelimiter.enums;

/**
 * 限流类型
 *
 * @author ycr
 */
public enum LimitType {

    /** 默认：全局共享一个令牌桶（Redisson OVERALL） */
    DEFAULT,

    /** 按客户端 IP 各自限流 */
    IP,

    /** 集群：按 Redisson 客户端实例限流（PER_CLIENT） */
    CLUSTER
}
