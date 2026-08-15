package com.ycr.framework.crud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ycr.framework.data.model.PageQuery;
import com.ycr.framework.data.model.PageResult;

import java.io.Serializable;
import java.util.List;

/**
 * 通用 CRUD 业务接口
 *
 * <p>与查询 DTO 解耦：{@code page/list} 收已构建的 {@link QueryWrapper}，由 Controller 负责从查询对象转换。</p>
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型
 * @author ycr
 */
public interface CrudService<T, ID extends Serializable> {

    /** 分页查询 */
    PageResult<T> page(PageQuery pageQuery, QueryWrapper<T> wrapper);

    /** 列表查询 */
    List<T> list(QueryWrapper<T> wrapper);

    /** 按主键查详情 */
    T get(ID id);

    /** 新增，返回是否成功 */
    boolean create(T entity);

    /** 修改，返回是否成功 */
    boolean update(T entity);

    /** 按主键删除，返回是否成功 */
    boolean delete(ID id);
}
