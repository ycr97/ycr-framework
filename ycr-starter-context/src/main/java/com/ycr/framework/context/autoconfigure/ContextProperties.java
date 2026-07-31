package com.ycr.framework.context.autoconfigure;

import com.ycr.framework.context.constant.ContextHeaderConstants;
import com.ycr.framework.context.enums.SecurityMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 上下文模块配置
 *
 * @author ycr
 */
@Data
@ConfigurationProperties(prefix = "ycr.context")
public class ContextProperties {

    /**
     * 是否信任上游（网关）通过 HTTP Header 透传的身份信息，并据此还原用户/租户/应用上下文。
     *
     * <p>默认 {@code false}：仅在确认本服务部署于受信任网关之后、外部无法直接伪造请求头时才应开启，
     * 否则存在身份伪造风险。无论该开关如何，请求结束时都会清理上下文以避免线程复用导致的串号。</p>
     */
    @Deprecated
    private boolean trustHeaders = false;

    /** 请求上下文安全模式 */
    private SecurityMode securityMode = SecurityMode.TOKEN_VERIFY;

    /** 上下文头签名配置 */
    private HeaderSign headerSign = new HeaderSign();

    /**
     * 获取兼容旧配置后的有效安全模式。
     */
    public SecurityMode effectiveSecurityMode() {
        if (trustHeaders && securityMode == SecurityMode.TOKEN_VERIFY) {
            return SecurityMode.GATEWAY_TRUST;
        }
        return securityMode;
    }

    /**
     * 上下文签名配置。
     */
    @Data
    public static class HeaderSign {

        /** 是否启用上下文头签名校验 */
        private boolean enabled = true;

        /** HMAC 签名密钥 */
        private String secret;

        /** 签名有效期 */
        private Duration ttl = Duration.ofSeconds(60);

        /** 时间戳 Header */
        private String timestampHeader = ContextHeaderConstants.HEADER_CONTEXT_TIMESTAMP;

        /** nonce Header */
        private String nonceHeader = ContextHeaderConstants.HEADER_CONTEXT_NONCE;

        /** 签名 Header */
        private String signatureHeader = ContextHeaderConstants.HEADER_CONTEXT_SIGNATURE;

        /** 签名无效时是否直接拒绝请求 */
        private boolean rejectInvalid = true;

        /** Redis nonce 键前缀 */
        private String replayKeyPrefix = "ycr:context:replay:";
    }
}
