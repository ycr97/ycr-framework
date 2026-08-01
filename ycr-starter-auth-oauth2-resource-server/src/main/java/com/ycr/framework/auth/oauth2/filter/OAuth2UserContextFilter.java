package com.ycr.framework.auth.oauth2.filter;

import com.ycr.framework.auth.oauth2.mapper.OAuth2ClaimsMappingException;
import com.ycr.framework.auth.oauth2.mapper.OAuth2UserContextMapper;
import com.ycr.framework.context.autoconfigure.ContextProperties;
import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.context.security.UserContextIdentityVerifier;
import com.ycr.framework.context.servlet.ServletContextBinder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 将 Spring Security Resource Server 认证结果桥接到 YCR UserContext。
 *
 * @author ycr
 */
public class OAuth2UserContextFilter extends OncePerRequestFilter {

    private final OAuth2UserContextMapper mapper;

    private final ServletContextBinder contextBinder;

    private final ContextProperties contextProperties;

    private final AuthenticationEntryPoint authenticationEntryPoint;

    public OAuth2UserContextFilter(OAuth2UserContextMapper mapper,
                                   ServletContextBinder contextBinder,
                                   ContextProperties contextProperties,
                                   AuthenticationEntryPoint authenticationEntryPoint) {
        this.mapper = mapper;
        this.contextBinder = contextBinder;
        this.contextProperties = contextProperties;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isOAuth2Authentication(authentication)) {
            if (hasUnexpectedExistingContext()) {
                contextBinder.clear();
                reject(request, response, new BadCredentialsException("unexpected YCR authentication context"));
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        boolean installed = false;
        try {
            UserContext tokenContext = map(authentication);
            UserContext existingContext = UserContextHolder.get();
            if (existingContext == null) {
                contextBinder.bind(tokenContext, request);
                installed = true;
            } else if (isMixedGatewayContext(existingContext)) {
                UserContextIdentityVerifier.verifyCompatible(existingContext, tokenContext);
            } else {
                throw new OAuth2ClaimsMappingException("已有 YCR 上下文来源不受信任");
            }
        } catch (RuntimeException failure) {
            contextBinder.clear();
            reject(request, response, failure);
            return;
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (installed) {
                contextBinder.clear();
            }
        }
    }

    private UserContext map(Authentication authentication) {
        Map<String, Object> claims;
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            claims = jwtAuthentication.getToken().getClaims();
        } else if (authentication instanceof BearerTokenAuthentication opaqueAuthentication) {
            claims = opaqueAuthentication.getTokenAttributes();
        } else {
            throw new OAuth2ClaimsMappingException("不支持的 OAuth2 认证类型");
        }
        UserContext userContext = mapper.map(claims);
        if (userContext == null) {
            throw new OAuth2ClaimsMappingException("OAuth2 claims 未能形成 UserContext");
        }
        userContext.setSource(UserContextSource.TOKEN.name());
        return userContext;
    }

    private boolean isOAuth2Authentication(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && (authentication instanceof JwtAuthenticationToken
                || authentication instanceof BearerTokenAuthentication);
    }

    private boolean hasUnexpectedExistingContext() {
        UserContext existingContext = UserContextHolder.get();
        return existingContext != null
                && !UserContextSource.GATEWAY_HEADER.name().equals(existingContext.getSource());
    }

    private boolean isMixedGatewayContext(UserContext existingContext) {
        return contextProperties.effectiveSecurityMode() == SecurityMode.MIXED
                && UserContextSource.GATEWAY_HEADER.name().equals(existingContext.getSource());
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, RuntimeException failure)
            throws IOException, ServletException {
        authenticationEntryPoint.commence(request, response,
                new BadCredentialsException("OAuth2 claims could not be mapped", failure));
    }
}
