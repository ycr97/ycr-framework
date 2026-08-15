package com.ycr.framework.common.model;

import lombok.Data;

import java.util.List;

/**
 * 树节点基类（按 code / parentCode 组织）
 *
 * @param <T>  子节点类型（通常为自身的子类）
 * @param <ID> 编码类型
 * @author ycr
 */
@Data
public class BaseTreeCodeDTO<T, ID> {

    /** 编码 */
    private ID code;

    /** 父级编码，顶层节点为空 */
    private ID parentCode;

    /** 子节点列表 */
    private List<T> children;
}
