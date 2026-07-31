package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.enums.SecurityMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ContextProperties 配置测试。
 *
 * @author ycr
 */
class ContextPropertiesTest {

    @Test
    @DisplayName("默认配置应使用tokenVerify和签名校验")
    void shouldMatchExpectedBehavior001() {
        ContextProperties properties = new ContextProperties();

        assertEquals(SecurityMode.TOKEN_VERIFY, properties.effectiveSecurityMode());
        assertTrue(properties.getHeaderSign().isEnabled());
        assertEquals(Duration.ofSeconds(60), properties.getHeaderSign().getTtl());
        assertEquals(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE,
                properties.getHeaderSign().getSignatureHeader());
    }

    @Test
    @DisplayName("旧trustHeaders开启时兼容映射为gatewayTrust")
    void shouldMatchExpectedBehavior002() {
        ContextProperties properties = new ContextProperties();
        properties.setTrustHeaders(true);

        assertEquals(SecurityMode.GATEWAY_TRUST, properties.effectiveSecurityMode());
    }
}
