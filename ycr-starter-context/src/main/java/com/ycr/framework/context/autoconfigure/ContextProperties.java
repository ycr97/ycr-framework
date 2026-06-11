package com.ycr.framework.context.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
    private boolean trustHeaders = false;
}
