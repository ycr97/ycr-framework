package com.ycr.framework.protect.xss;

import com.ycr.framework.protect.xss.enums.XssMode;

import java.util.regex.Pattern;

/**
 * XSS 清理工具
 *
 * <p>{@link XssMode#ESCAPE} 把危险字符转为 HTML 实体（无损，推荐）；{@link XssMode#CLEAN} 逐层移除
 * 脚本/样式块、事件处理器、危险协议与所有标签（有损）。CLEAN 为正则实现，作为输入侧的纵深防御之一，
 * 不应作为唯一防线——输出渲染时仍应做转义。</p>
 *
 * @author ycr
 */
public final class XssCleaner {

    private static final Pattern SCRIPT_BLOCK =
            Pattern.compile("<\\s*(script|style)[^>]*>.*?<\\s*/\\s*\\1\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EVENT_HANDLER =
            Pattern.compile("\\son\\w+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DANGEROUS_SCHEME =
            Pattern.compile("(javascript|vbscript|data)\\s*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ANY_TAG = Pattern.compile("<[^>]*>");

    private XssCleaner() {
    }

    /**
     * 按模式清理输入。
     *
     * @param value 原始值（可为 null）
     * @param mode  处理模式
     * @return 处理后的值；入参为 null/空原样返回
     */
    public static String sanitize(String value, XssMode mode) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return mode == XssMode.CLEAN ? clean(value) : escape(value);
    }

    /** 转义危险字符为 HTML 实体 */
    public static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#x27;");
                case '/' -> sb.append("&#x2F;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 逐层移除脚本块、事件处理器、危险协议与所有标签 */
    public static String clean(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String result = SCRIPT_BLOCK.matcher(value).replaceAll("");
        result = EVENT_HANDLER.matcher(result).replaceAll("");
        result = DANGEROUS_SCHEME.matcher(result).replaceAll("");
        result = ANY_TAG.matcher(result).replaceAll("");
        return result.trim();
    }
}
