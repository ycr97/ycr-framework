package com.ycr.framework.crud.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ycr.framework.core.model.R;
import com.ycr.framework.data.model.BaseDO;
import com.ycr.framework.data.model.PageQuery;
import com.ycr.framework.data.model.PageResult;
import com.ycr.framework.data.mp.util.QueryWrapperHelper;
import com.ycr.framework.crud.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.List;

/**
 * 通用 CRUD 控制器基类
 *
 * <p>子类声明 {@code @RestController @RequestMapping("/x")} 继承本类即获全套端点；
 * 用 {@code @CrudApi(disable = {...})} 关闭指定端点。增删改查直接对实体 {@code T}（薄版 DO 直通）。</p>
 *
 * @param <T>  实体类型（继承 {@link BaseDO}）
 * @param <ID> 主键类型
 * @param <Q>  查询对象类型（字段以 {@code @Query} 标注）
 * @author ycr
 */
public abstract class AbstractCrudController<T extends BaseDO, ID extends Serializable, Q> {

    @Autowired
    protected CrudService<T, ID> crudService;

    /** 分页查询 */
    @GetMapping("/page")
    public R<PageResult<T>> page(Q query, PageQuery pageQuery) {
        QueryWrapper<T> wrapper = QueryWrapperHelper.build(query);
        return R.ok(crudService.page(pageQuery, wrapper));
    }

    /** 列表查询 */
    @GetMapping("/list")
    public R<List<T>> list(Q query) {
        QueryWrapper<T> wrapper = QueryWrapperHelper.build(query);
        return R.ok(crudService.list(wrapper));
    }

    /** 详情 */
    @GetMapping("/{id}")
    public R<T> get(@PathVariable ID id) {
        return R.ok(crudService.get(id));
    }

    /** 新增，data 为是否成功（影响行数 &gt; 0） */
    @PostMapping
    public R<Boolean> create(@RequestBody T entity) {
        return R.ok(crudService.create(entity));
    }

    /** 修改，data 为是否成功；影响 0 行（如 id 不存在）时 data=false */
    @PutMapping
    public R<Boolean> update(@RequestBody T entity) {
        return R.ok(crudService.update(entity));
    }

    /** 删除，data 为是否成功；影响 0 行（如 id 不存在）时 data=false */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable ID id) {
        return R.ok(crudService.delete(id));
    }
}
