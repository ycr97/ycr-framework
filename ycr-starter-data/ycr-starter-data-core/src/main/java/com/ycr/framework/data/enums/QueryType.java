package com.ycr.framework.data.enums;

/**
 * 查询类型枚举
 *
 * @author ycr
 */
public enum QueryType {
    /** 等于 */
    EQ,
    /** 不等于 */
    NE,
    /** 大于 */
    GT,
    /** 大于等于 */
    GE,
    /** 小于 */
    LT,
    /** 小于等于 */
    LE,
    /** 模糊匹配 */
    LIKE,
    /** 左模糊 */
    LIKE_LEFT,
    /** 右模糊 */
    LIKE_RIGHT,
    /** IN 查询 */
    IN,
    /** BETWEEN 查询 */
    BETWEEN,
    /** IS NULL */
    IS_NULL,
    /** IS NOT NULL */
    IS_NOT_NULL
}
