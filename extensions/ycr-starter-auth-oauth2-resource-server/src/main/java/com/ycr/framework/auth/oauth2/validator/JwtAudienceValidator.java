package com.ycr.framework.auth.oauth2.validator;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * JWT audience 精确匹配校验器。
 *
 * @author ycr
 */
public final class JwtAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6750#section-3.1";

    private final Set<String> expectedAudiences;

    public JwtAudienceValidator(Collection<String> expectedAudiences) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (expectedAudiences != null) {
            expectedAudiences.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .forEach(normalized::add);
        }
        this.expectedAudiences = Collections.unmodifiableSet(normalized);
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (token != null && token.getAudience() != null
                && token.getAudience().stream().anyMatch(expectedAudiences::contains)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                "invalid_token",
                "The token audience is invalid",
                ERROR_URI));
    }
}
