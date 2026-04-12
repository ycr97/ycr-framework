package com.ycr.framework.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 * @author ycr
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    /** 数据列表 */
    private List<T> list;

    /** 总条数 */
    private long total;

    /** 当前页码 */
    private int page;

    /** 每页条数 */
    private int size;
}
