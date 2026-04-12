package com.ycr.framework.context.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 租户上下文
 *
 * @author ycr
 */
@Data
public class TenantContext implements Serializable {

    private Long tenantId;

    private String tenantCode;

    private String tenantName;
}
