package com.ycr.framework.data.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数
 *
 * @author ycr
 */
@Data
public class PageQuery implements Serializable {

    /** 页码（从1开始） */
    @Min(value = 1, message = "页码最小为1")
    private int page = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 1000, message = "每页条数最大为1000")
    private int size = 10;

    /** 排序字段 */
    private String sortField;

    /** 排序方向（asc/desc） */
    private String sortOrder;
}
