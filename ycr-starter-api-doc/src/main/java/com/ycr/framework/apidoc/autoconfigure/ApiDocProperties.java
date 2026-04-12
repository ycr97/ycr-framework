package com.ycr.framework.apidoc.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API 文档配置属性
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.api-doc")
public class ApiDocProperties {

    private boolean enabled = true;

    private String title = "API 文档";

    private String description = "";

    private String version = "1.0.0";

    private String contactName = "";

    private String contactEmail = "";
}
