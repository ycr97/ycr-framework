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

    /** 是否启用安全拦截（注册 SaToken 注解鉴权拦截器），默认启用 */
    private boolean enabled = true;

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
