package com.ycr.framework.auth.oauth2.autoconfigure;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * OAuth2 Resource Server 启动配置校验。
 *
 * @author ycr
 */
final class OAuth2ResourceServerPropertiesValidator {

    private static final String PREFIX = "ycr.auth.oauth2.resource-server";

    private final OAuth2ResourceServerProperties properties;

    private final Environment environment;

    OAuth2ResourceServerPropertiesValidator(OAuth2ResourceServerProperties properties,
                                             Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    void validate() {
        if (environment.getProperty("ycr.auth.satoken.enabled", Boolean.class, false)) {
            throw new IllegalStateException(
                    "ycr.auth.satoken.enabled and ycr.auth.oauth2.resource-server.enabled cannot both be true");
        }

        String securityMode = environment.getProperty("ycr.context.security-mode", "");
        boolean legacyTrustHeaders = environment.getProperty("ycr.context.trust-headers", Boolean.class, false);
        if ("GATEWAY_TRUST".equalsIgnoreCase(securityMode) || legacyTrustHeaders) {
            throw new IllegalStateException(
                    "ycr.context.security-mode=GATEWAY_TRUST cannot be used with "
                            + "ycr.auth.oauth2.resource-server.enabled");
        }

        if (properties.getMode() == null) {
            throw new IllegalStateException(PREFIX + ".mode is required when " + PREFIX + ".enabled=true");
        }
        if (properties.getMode() == OAuth2ResourceServerProperties.TokenMode.JWT) {
            validateJwt();
        } else {
            validateOpaque();
        }
    }

    private void validateJwt() {
        if (!StringUtils.hasText(properties.getJwt().getIssuerUri())) {
            throw new IllegalStateException(PREFIX + ".jwt.issuer-uri is required when mode=jwt");
        }
        requireAudiences(properties.getJwt().getAudiences(), PREFIX + ".jwt.audiences");
        List<String> algorithms = properties.getJwt().getAllowedAlgorithms();
        if (algorithms == null || algorithms.stream().noneMatch(StringUtils::hasText)) {
            throw new IllegalStateException(PREFIX + ".jwt.allowed-algorithms must not be empty when mode=jwt");
        }
        for (String algorithm : algorithms) {
            if (!StringUtils.hasText(algorithm)) {
                continue;
            }
            String normalized = algorithm.trim();
            if ("none".equalsIgnoreCase(normalized)
                    || normalized.toUpperCase().startsWith("HS")
                    || normalized.toUpperCase().startsWith("HMAC")) {
                throw new IllegalStateException(
                        PREFIX + ".jwt.allowed-algorithms must not contain symmetric or none algorithms");
            }
        }
        if (properties.getJwt().getClockSkew() == null || properties.getJwt().getClockSkew().isNegative()) {
            throw new IllegalStateException(PREFIX + ".jwt.clock-skew must not be negative");
        }
    }

    private void validateOpaque() {
        if (!StringUtils.hasText(properties.getOpaque().getIntrospectionUri())) {
            throw new IllegalStateException(PREFIX + ".opaque.introspection-uri is required when mode=opaque");
        }
        if (!StringUtils.hasText(properties.getOpaque().getClientId())) {
            throw new IllegalStateException(PREFIX + ".opaque.client-id is required when mode=opaque");
        }
        if (!StringUtils.hasText(properties.getOpaque().getClientSecret())) {
            throw new IllegalStateException(PREFIX + ".opaque.client-secret is required when mode=opaque");
        }
        requireAudiences(properties.getOpaque().getAudiences(), PREFIX + ".opaque.audiences");
        if (properties.getOpaque().getConnectTimeout() == null
                || properties.getOpaque().getConnectTimeout().isZero()
                || properties.getOpaque().getConnectTimeout().isNegative()) {
            throw new IllegalStateException(PREFIX + ".opaque.connect-timeout must be positive");
        }
        if (properties.getOpaque().getReadTimeout() == null
                || properties.getOpaque().getReadTimeout().isZero()
                || properties.getOpaque().getReadTimeout().isNegative()) {
            throw new IllegalStateException(PREFIX + ".opaque.read-timeout must be positive");
        }
    }

    private void requireAudiences(List<String> audiences, String property) {
        if (audiences == null || audiences.stream().noneMatch(StringUtils::hasText)) {
            throw new IllegalStateException(property + " must not be empty");
        }
    }
}
