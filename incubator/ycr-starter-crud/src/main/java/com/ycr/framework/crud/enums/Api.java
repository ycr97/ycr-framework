package com.ycr.framework.crud.enums;

/**
 * CRUD 操作枚举
 *
 * <p>与 {@code AbstractCrudController} 的基类方法一一对应，用于 {@code @CrudApi} 关闭指定端点。</p>
 *
 * @author ycr
 */
public enum Api {

    /** 分页查询 {@code GET /page} */
    PAGE,
    /** 列表查询 {@code GET /list} */
    LIST,
    /** 详情 {@code GET /{id}} */
    GET,
    /** 新增 {@code POST} */
    CREATE,
    /** 修改 {@code PUT} */
    UPDATE,
    /** 删除 {@code DELETE /{id}} */
    DELETE
}
