package com.ycr.framework.context.model;

import lombok.Data;

import java.io.Serializable;

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

    private String roles;

    private Long deptId;
}
