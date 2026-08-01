package com.ycr.framework.auth.oauth2.autoconfigure;

import com.ycr.framework.auth.oauth2.validator.JwtAudienceValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * OAuth2 Resource Server JWT 自动配置。
 *
 * @author ycr
 */
@AutoConfiguration(after = OAuth2ResourceServerAutoConfiguration.class)
@ConditionalOnProperty(prefix = "ycr.auth.oauth2.resource-server", name = "enabled", havingValue = "true")
public class OAuth2JwtAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ycr.auth.oauth2.resource-server", name = "mode", havingValue = "jwt")
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder oauth2JwtDecoder(OAuth2ResourceServerProperties properties) {
        OAuth2ResourceServerProperties.Jwt jwt = properties.getJwt();
        NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder builder = StringUtils.hasText(jwt.getJwkSetUri())
                ? NimbusJwtDecoder.withJwkSetUri(jwt.getJwkSetUri())
                : NimbusJwtDecoder.withIssuerLocation(jwt.getIssuerUri());

        List<String> allowedAlgorithms = jwt.getAllowedAlgorithms();
        if (allowedAlgorithms == null || allowedAlgorithms.stream().noneMatch(StringUtils::hasText)) {
            throw new IllegalStateException(
                    "ycr.auth.oauth2.resource-server.jwt.allowed-algorithms must not be empty when mode=jwt");
        }
        builder.jwsAlgorithms(algorithms -> allowedAlgorithms.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(SignatureAlgorithm::from)
                .forEach(algorithms::add));

        NimbusJwtDecoder decoder = builder.build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtIssuerValidator(jwt.getIssuerUri()),
                new JwtTimestampValidator(jwt.getClockSkew()),
                new JwtAudienceValidator(jwt.getAudiences()));
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
