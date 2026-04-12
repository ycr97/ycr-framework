package com.ycr.framework.data.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据库实体基类
 *
 * @author ycr
 */
@Getter
@Setter
public abstract class BaseDO implements Serializable {

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;

    /** 创建人ID */
    private Long createUser;

    /** 修改人ID */
    private Long updateUser;
}
