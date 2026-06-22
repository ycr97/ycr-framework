package com.ycr.framework.context.resolver;

import com.ycr.framework.context.enums.SecurityMode;
import com.ycr.framework.context.enums.UserContextSource;
import com.ycr.framework.context.exception.ContextAuthException;
import com.ycr.framework.context.model.UserContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * UserContextResolverChain 测试。
 *
 * @author ycr
 */
class UserContextResolverChainTest {

    @Test
    void mixed模式签名上下文优先于token() {
        UserContextResolverChain chain = new UserContextResolverChain(List.of(
                resolver(UserContextSource.GATEWAY_HEADER, 1L),
                resolver(UserContextSource.TOKEN, 1L)));

        UserContext userContext = chain.resolve(new UserContextResolveRequest(
                new MockHttpServletRequest(), SecurityMode.MIXED, "trace"));

        assertEquals(UserContextSource.GATEWAY_HEADER.name(), userContext.getSource());
    }

    @Test
    void mixed模式身份冲突时拒绝() {
        UserContextResolverChain chain = new UserContextResolverChain(List.of(
                resolver(UserContextSource.GATEWAY_HEADER, 1L),
                resolver(UserContextSource.TOKEN, 2L)));

        assertThrows(ContextAuthException.class, () -> chain.resolve(new UserContextResolveRequest(
                new MockHttpServletRequest(), SecurityMode.MIXED, "trace")));
    }

    private UserContextResolver resolver(UserContextSource source, Long userId) {
        return new UserContextResolver() {
            @Override
            public boolean supports(UserContextResolveRequest request) {
                return true;
            }

            @Override
            public UserContext resolve(UserContextResolveRequest request) {
                UserContext userContext = new UserContext();
                userContext.setUserId(userId);
                userContext.setSource(source.name());
                return userContext;
            }
        };
    }
}
