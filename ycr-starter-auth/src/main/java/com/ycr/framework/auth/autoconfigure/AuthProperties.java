package com.ycr.framework.auth.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证配置属性
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.auth")
public class AuthProperties {

    /** 放行路径列表 */
    private List<String> excludePaths = new ArrayList<>();
}
