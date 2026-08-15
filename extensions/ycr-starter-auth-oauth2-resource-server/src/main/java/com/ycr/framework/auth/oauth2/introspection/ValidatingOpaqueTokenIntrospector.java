package com.ycr.framework.auth.oauth2.introspection;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 对 introspection 结果执行 audience 和 issuer 二次校验的包装器。
 *
 * @author ycr
 */
public final class ValidatingOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final OpaqueTokenIntrospector delegate;

    private final Set<String> expectedAudiences;

    private final String expectedIssuer;

    public ValidatingOpaqueTokenIntrospector(OpaqueTokenIntrospector delegate,
                                             Collection<String> expectedAudiences,
                                             String expectedIssuer) {
        Assert.notNull(delegate, "delegate cannot be null");
        this.delegate = delegate;

        LinkedHashSet<String> normalizedAudiences = new LinkedHashSet<>();
        if (expectedAudiences != null) {
            expectedAudiences.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .forEach(normalizedAudiences::add);
        }
        this.expectedAudiences = Collections.unmodifiableSet(normalizedAudiences);
        this.expectedIssuer = StringUtils.hasText(expectedIssuer) ? expectedIssuer.trim() : null;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2AuthenticatedPrincipal principal;
        try {
            principal = delegate.introspect(token);
        } catch (BadOpaqueTokenException e) {
            throw new BadOpaqueTokenException("Opaque token rejected");
        } catch (OAuth2IntrospectionException e) {
            throw new OAuth2IntrospectionException("Opaque token introspection unavailable");
        }
        if (principal == null) {
            throw new OAuth2IntrospectionException("Introspection endpoint returned no principal");
        }

        Map<String, Object> attributes = principal.getAttributes();
        if (!matchesAudience(attributes.get(OAuth2TokenIntrospectionClaimNames.AUD))) {
            throw new BadOpaqueTokenException("Opaque token audience is invalid");
        }
        if (expectedIssuer != null && !matchesIssuer(attributes.get(OAuth2TokenIntrospectionClaimNames.ISS))) {
            throw new BadOpaqueTokenException("Opaque token issuer is invalid");
        }
        return principal;
    }

    private boolean matchesAudience(Object value) {
        if (value instanceof CharSequence text) {
            return expectedAudiences.contains(text.toString());
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().anyMatch(this::matchesAudienceValue);
        }
        if (value != null && value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                if (matchesAudienceValue(Array.get(value, i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesAudienceValue(Object value) {
        return value instanceof CharSequence text && expectedAudiences.contains(text.toString());
    }

    private boolean matchesIssuer(Object value) {
        return value instanceof CharSequence issuer && expectedIssuer.equals(issuer.toString());
    }
}
