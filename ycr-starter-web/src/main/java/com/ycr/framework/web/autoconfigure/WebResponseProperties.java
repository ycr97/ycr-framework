package com.ycr.framework.web.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一响应包装配置。
 *
 * @author ycr
 */
@ConfigurationProperties(prefix = "ycr.web.response")
public class WebResponseProperties {

    /**
     * 是否启用统一响应包装。
     */
    private boolean enabled = false;

    /**
     * 需要包装的路径，默认全部接口。
     */
    private List<String> includePaths = new ArrayList<>(List.of("/**"));

    /**
     * 不需要包装的路径，优先级高于 includePaths。
     */
    private List<String> excludePaths = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getIncludePaths() {
        return includePaths;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.includePaths = includePaths;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }
}
