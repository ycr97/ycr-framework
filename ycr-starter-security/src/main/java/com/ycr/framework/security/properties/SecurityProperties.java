package com.ycr.framework.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 安全配置属性
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.security")
public class SecurityProperties {

    /** 放行路径列表（不需要认证即可访问） */
    private List<String> excludePaths = new ArrayList<>(Arrays.asList(
            "/doc.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/error",
            "/actuator/**"
    ));
}
