package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.enums.SecurityMode;
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
    void 默认配置应使用tokenVerify和签名校验() {
        ContextProperties properties = new ContextProperties();

        assertEquals(SecurityMode.TOKEN_VERIFY, properties.effectiveSecurityMode());
        assertTrue(properties.getHeaderSign().isEnabled());
        assertEquals(Duration.ofSeconds(60), properties.getHeaderSign().getTtl());
        assertEquals(ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE,
                properties.getHeaderSign().getSignatureHeader());
    }

    @Test
    void 旧trustHeaders开启时兼容映射为gatewayTrust() {
        ContextProperties properties = new ContextProperties();
        properties.setTrustHeaders(true);

        assertEquals(SecurityMode.GATEWAY_TRUST, properties.effectiveSecurityMode());
    }
}
