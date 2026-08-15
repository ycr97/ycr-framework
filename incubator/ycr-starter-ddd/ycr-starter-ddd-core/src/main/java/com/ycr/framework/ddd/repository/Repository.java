package com.ycr.framework.ddd.repository;

import java.util.Optional;

/**
 * 仓储接口 —— 聚合根持久化抽象，不绑定具体 ORM
 *
 * @param <A>  聚合根类型
 * @param <ID> 标识类型
 * @author ycr
 */
public interface Repository<A, ID> {

    /**
     * 保存聚合根，返回保存后的实例
     */
    A save(A aggregate);

    /**
     * 按标识查找
     */
    Optional<A> findById(ID id);

    /**
     * 删除聚合根
     */
    void remove(A aggregate);

    /**
     * 按标识删除
     */
    void removeById(ID id);
}
