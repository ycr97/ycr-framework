package com.ycr.framework.context.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 用户上下文
 *
 * @author ycr
 */
@Data
public class UserContext implements Serializable {

    private Long userId;

    private String username;

    private String nickname;

    private Long tenantId;

    private Long deptId;

    private Set<String> roles;

    private Set<String> permissions;

    private String clientId;

    private String source;
}
