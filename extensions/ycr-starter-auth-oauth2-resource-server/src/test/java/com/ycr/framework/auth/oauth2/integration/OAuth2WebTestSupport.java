package com.ycr.framework.auth.oauth2.integration;

import com.ycr.framework.auth.oauth2.validator.JwtAudienceValidator;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class OAuth2WebTestSupport {

    static final String ISSUER = "https://idp.example.com";

    static final String AUDIENCE = "order-api";

    static final String HEADER_SECRET = "mixed-header-secret";

    static final RSAKey SIGNING_KEY = generateKey("test-key");

    static final RSAKey OTHER_SIGNING_KEY = generateKey("other-key");

    private OAuth2WebTestSupport() {
    }

    static JwtDecoder jwtDecoder() {
        try {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(SIGNING_KEY.toRSAPublicKey()).build();
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    new JwtIssuerValidator(ISSUER),
                    new JwtTimestampValidator(),
                    new JwtAudienceValidator(List.of(AUDIENCE))));
            return decoder;
        } catch (JOSEException e) {
            throw new IllegalStateException("测试 RSA 公钥初始化失败", e);
        }
    }

    static String validToken() {
        return token(SIGNING_KEY, "alice", ISSUER, List.of(AUDIENCE),
                Instant.now().minusSeconds(5), Instant.now().plusSeconds(300),
                Map.of(
                        "user_id", 1001L,
                        "preferred_username", "alice",
                        "tenant_id", 42L,
                        "client_id", "web",
                        "roles", List.of("user"),
                        "permissions", List.of("order:read"),
                        "scope", "profile order:read"));
    }

    static String token(RSAKey signingKey, String subject, String issuer, List<String> audience,
                        Instant issuedAt, Instant expiresAt, Map<String, Object> additionalClaims) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(audience)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt);
        if (subject != null) {
            claims.subject(subject);
        }
        if (additionalClaims != null) {
            additionalClaims.forEach(claims::claim);
        }
        return encoder(signingKey).encode(JwtEncoderParameters.from(
                        JwsHeader.with(SignatureAlgorithm.RS256).keyId(signingKey.getKeyID()).build(),
                        claims.build()))
                .getTokenValue();
    }

    static String tokenWithNotBefore(Instant notBefore) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject("alice")
                .audience(List.of(AUDIENCE))
                .issuedAt(Instant.now().minusSeconds(5))
                .notBefore(notBefore)
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("user_id", 1001L)
                .claim("preferred_username", "alice")
                .claim("tenant_id", 42L)
                .claim("permissions", List.of("order:read"));
        return encoder(SIGNING_KEY).encode(JwtEncoderParameters.from(
                        JwsHeader.with(SignatureAlgorithm.RS256).keyId(SIGNING_KEY.getKeyID()).build(),
                        claims.build()))
                .getTokenValue();
    }

    static String tokenWithClaims(Map<String, Object> claims) {
        Map<String, Object> allClaims = new LinkedHashMap<>();
        allClaims.put("user_id", 1001L);
        allClaims.put("preferred_username", "alice");
        allClaims.put("tenant_id", 42L);
        allClaims.put("client_id", "web");
        allClaims.put("permissions", List.of("order:read"));
        allClaims.putAll(claims);
        return token(SIGNING_KEY, "alice", ISSUER, List.of(AUDIENCE),
                Instant.now().minusSeconds(5), Instant.now().plusSeconds(300), allClaims);
    }

    private static NimbusJwtEncoder encoder(RSAKey signingKey) {
        JWKSource<SecurityContext> source = (selector, context) -> selector.select(new JWKSet(signingKey));
        return new NimbusJwtEncoder(source);
    }

    private static RSAKey generateKey(String keyId) {
        try {
            return new RSAKeyGenerator(2048).keyID(keyId).generate();
        } catch (JOSEException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
