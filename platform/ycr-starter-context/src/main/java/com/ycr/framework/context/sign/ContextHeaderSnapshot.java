package com.ycr.framework.context.sign;

import lombok.Data;

/**
 * 参与上下文签名的 Header 快照。
 *
 * @author ycr
 */
@Data
public class ContextHeaderSnapshot {

    private String method;

    private String path;

    private String audience;

    private String timestamp;

    private String nonce;

    private String userId;

    private String username;

    private String nickname;

    private String tenantId;

    private String tenantCode;

    private String deptId;

    private String roles;

    private String permissions;

    private String clientId;

    private String appId;

    private String traceId;
}
