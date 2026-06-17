package com.ycr.framework.common.model;

import lombok.Data;

import java.util.List;

/**
 * 树节点基类（按 id / parentId 组织）
 *
 * @param <T>  子节点类型（通常为自身的子类）
 * @param <ID> 主键类型
 * @author ycr
 */
@Data
public class BaseTreeDTO<T, ID> {

    /** 主键 ID */
    private ID id;

    /** 父级 ID，顶层节点为空 */
    private ID parentId;

    /** 子节点列表 */
    private List<T> children;
}
