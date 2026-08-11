package com.ycr.framework.apidoc.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/** 将 YCR 文档总开关映射为 SpringDoc/Knife4j 的早期装配开关。 */
public class ApiDocEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String PROPERTY_SOURCE_NAME = "ycrApiDocMasterSwitch";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty("ycr.api-doc.enabled", Boolean.class, true)) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, Map.of(
                    "springdoc.api-docs.enabled", false,
                    "springdoc.swagger-ui.enabled", false,
                    "knife4j.enable", false)));
        }
    }

    @Override
    public int getOrder() {
        // ycr.api-doc.enabled may come from application.yml, so evaluate after Config Data is loaded.
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }
}
