package com.ycr.framework.web.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS 跨域配置属性
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.web.cors")
public class CorsProperties {

    private boolean enabled = false;
    private List<String> allowedOrigins = List.of("*");
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
    private List<String> allowedHeaders = List.of("*");
    private boolean allowCredentials = false;
    private long maxAge = 3600;
}
