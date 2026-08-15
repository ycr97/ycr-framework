package com.ycr.framework.auth.oauth2.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAudienceValidatorTest {

    @Test
    @DisplayName("aud至少命中一个配置值时校验通过")
    void acceptsAnyConfiguredAudience() {
        Jwt token = tokenWithAudience(List.of("profile", "order-api"));

        assertThat(new JwtAudienceValidator(List.of("order-api", "inventory-api"))
                .validate(token).hasErrors()).isFalse();
    }

    @Test
    @DisplayName("aud匹配必须大小写敏感")
    void audienceMatchIsCaseSensitive() {
        Jwt token = tokenWithAudience(List.of("Order-API"));

        assertThat(new JwtAudienceValidator(List.of("order-api"))
                .validate(token).hasErrors()).isTrue();
    }

    @Test
    @DisplayName("aud缺失或配置为空时校验失败")
    void rejectsMissingAudience() {
        Jwt token = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user-1")
                .build();

        assertThat(new JwtAudienceValidator(List.of()).validate(token).hasErrors()).isTrue();
        assertThat(new JwtAudienceValidator(List.of("order-api")).validate(token).hasErrors()).isTrue();
    }

    private Jwt tokenWithAudience(List<String> audience) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("aud", audience)
                .build();
    }
}
