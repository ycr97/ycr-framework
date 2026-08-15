package com.ycr.framework.auth.autoconfigure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 注入可被业务配置覆盖的 Sa-Token 安全默认值。
 *
 * @author ycr
 */
public class SaTokenDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String PROPERTY_SOURCE_NAME = "ycr-auth-satoken-defaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            return;
        }
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("sa-token.token-name", "Authorization");
        defaults.put("sa-token.token-prefix", "Bearer");
        defaults.put("sa-token.is-read-header", true);
        defaults.put("sa-token.is-read-body", false);
        defaults.put("sa-token.is-read-cookie", false);
        environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
    }
}
