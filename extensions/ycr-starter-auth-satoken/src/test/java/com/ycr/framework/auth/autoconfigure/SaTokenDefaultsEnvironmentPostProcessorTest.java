package com.ycr.framework.auth.autoconfigure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaTokenDefaultsEnvironmentPostProcessorTest {

    private final SaTokenDefaultsEnvironmentPostProcessor processor =
            new SaTokenDefaultsEnvironmentPostProcessor();

    @Test
    @DisplayName("应以最低优先级注入Bearer Header安全默认值")
    void shouldProvideSafeBearerHeaderDefaults() {
        MockEnvironment environment = new MockEnvironment();

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("sa-token.token-name")).isEqualTo("Authorization");
        assertThat(environment.getProperty("sa-token.token-prefix")).isEqualTo("Bearer");
        assertThat(environment.getProperty("sa-token.is-read-header", Boolean.class)).isTrue();
        assertThat(environment.getProperty("sa-token.is-read-body", Boolean.class)).isFalse();
        assertThat(environment.getProperty("sa-token.is-read-cookie", Boolean.class)).isFalse();
        List<String> propertySourceNames = environment.getPropertySources().stream()
                .map(propertySource -> propertySource.getName())
                .toList();
        assertThat(propertySourceNames.get(propertySourceNames.size() - 1))
                .isEqualTo(SaTokenDefaultsEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
    }

    @Test
    @DisplayName("业务配置应覆盖框架默认值")
    void applicationPropertyShouldOverrideFrameworkDefault() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("sa-token.token-name", "X-Access-Token");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("sa-token.token-name")).isEqualTo("X-Access-Token");
    }
}
