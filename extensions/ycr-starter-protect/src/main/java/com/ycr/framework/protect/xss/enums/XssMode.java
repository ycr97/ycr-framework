package com.ycr.framework.protect.xss.enums;

/**
 * XSS 处理模式
 *
 * @author ycr
 */
public enum XssMode {

    /** 转义：把 {@code < > & " ' /} 转为 HTML 实体，保留原始内容（推荐，无损） */
    ESCAPE,

    /** 清理：移除脚本块、事件处理器、危险协议与所有 HTML 标签（有损） */
    CLEAN
}
