package com.ycr.framework.context.resolver;

import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.exception.ContextAuthException;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.security.UserContextIdentityVerifier;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户上下文解析链。
 *
 * @author ycr
 */
public class UserContextResolverChain {

    private final List<UserContextResolver> resolvers;

    public UserContextResolverChain(List<UserContextResolver> resolvers) {
        this.resolvers = new ArrayList<>(resolvers == null ? List.of() : resolvers);
        AnnotationAwareOrderComparator.sort(this.resolvers);
    }

    /**
     * 按安全模式解析用户上下文。
     */
    public UserContext resolve(UserContextResolveRequest request) {
        if (request.getSecurityMode() == SecurityMode.MIXED) {
            return resolveMixed(request);
        }
        for (UserContextResolver resolver : resolvers) {
            if (!resolver.supports(request)) {
                continue;
            }
            UserContext userContext = resolver.resolve(request);
            if (userContext != null) {
                return userContext;
            }
        }
        return null;
    }

    private UserContext resolveMixed(UserContextResolveRequest request) {
        UserContext signed = null;
        UserContext token = null;
        for (UserContextResolver resolver : resolvers) {
            if (!resolver.supports(request)) {
                continue;
            }
            UserContext userContext = resolver.resolve(request);
            if (userContext == null) {
                continue;
            }
            if (UserContextSource.GATEWAY_HEADER.name().equals(userContext.getSource())) {
                signed = firstNonNull(signed, userContext);
            } else if (UserContextSource.TOKEN.name().equals(userContext.getSource())) {
                token = firstNonNull(token, userContext);
            }
        }
        if (signed != null && token != null) {
            UserContextIdentityVerifier.verifyCompatible(signed, token);
        }
        return signed != null ? signed : token;
    }

    private UserContext firstNonNull(UserContext current, UserContext candidate) {
        return current != null ? current : candidate;
    }

}
