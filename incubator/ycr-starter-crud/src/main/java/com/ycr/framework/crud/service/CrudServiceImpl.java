package com.ycr.framework.crud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ycr.framework.data.model.PageQuery;
import com.ycr.framework.data.model.PageResult;
import com.ycr.framework.data.mp.mapper.BaseMapperX;
import com.ycr.framework.data.mp.util.MpPageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

/**
 * 通用 CRUD 业务实现：基于 MyBatis-Plus {@link BaseMapperX} + {@link MpPageHelper}
 *
 * @param <M>  Mapper 类型
 * @param <T>  实体类型
 * @param <ID> 主键类型
 * @author ycr
 */
public class CrudServiceImpl<M extends BaseMapperX<T>, T, ID extends Serializable>
        implements CrudService<T, ID> {

    @Autowired
    protected M baseMapper;

    @Override
    public PageResult<T> page(PageQuery pageQuery, QueryWrapper<T> wrapper) {
        Page<T> page = MpPageHelper.toPage(pageQuery);
        return MpPageHelper.toResult(baseMapper.selectPage(page, wrapper));
    }

    @Override
    public List<T> list(QueryWrapper<T> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    @Override
    public T get(ID id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean create(T entity) {
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean update(T entity) {
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public boolean delete(ID id) {
        return baseMapper.deleteById(id) > 0;
    }
}
