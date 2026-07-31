package com.ycr.framework.protect.xss.autoconfigure;

import com.ycr.framework.protect.xss.enums.XssMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * XSS 过滤配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.protect.xss")
public class XssProperties {

    /** 是否启用 XSS 过滤，默认关闭 */
    private boolean enabled = false;

    /** 处理模式，默认 {@link XssMode#ESCAPE}（无损转义） */
    private XssMode mode = XssMode.ESCAPE;

    /**
     * 拦截路径（Ant 风格）；非空时仅这些路径过滤，留空表示全部过滤。
     */
    private List<String> includePatterns = new ArrayList<>();

    /**
     * 放行路径（Ant 风格），优先级高于 {@link #includePatterns}；命中则不过滤。
     */
    private List<String> excludePatterns = new ArrayList<>();
}
