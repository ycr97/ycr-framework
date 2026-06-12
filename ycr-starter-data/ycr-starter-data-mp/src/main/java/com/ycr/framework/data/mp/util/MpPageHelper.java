package com.ycr.framework.data.mp.util;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ycr.framework.data.model.PageQuery;
import com.ycr.framework.data.model.PageResult;

import java.util.regex.Pattern;

/**
 * 分页适配器：在框架的 {@link PageQuery}/{@link PageResult} 与 MyBatis-Plus 的
 * {@link Page}/{@link IPage} 之间转换。
 *
 * @author ycr
 */
public final class MpPageHelper {

    /** 排序列白名单：仅允许字母/数字/下划线，拦截 SQL 注入 */
    private static final Pattern SAFE_COLUMN = Pattern.compile("^[A-Za-z0-9_]+$");

    private MpPageHelper() {
    }

    /**
     * {@link PageQuery} → MP {@link Page}，含排序（字段名转下划线列名并做白名单校验）
     *
     * @throws IllegalArgumentException 排序字段非法（含注入风险字符）时
     */
    public static <T> Page<T> toPage(PageQuery pageQuery) {
        Page<T> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        if (StrUtil.isNotBlank(pageQuery.getSortField())) {
            String column = StrUtil.toUnderlineCase(pageQuery.getSortField());
            if (!SAFE_COLUMN.matcher(column).matches()) {
                throw new IllegalArgumentException("非法排序字段: " + pageQuery.getSortField());
            }
            boolean asc = !"desc".equalsIgnoreCase(pageQuery.getSortOrder());
            page.addOrder(asc ? OrderItem.asc(column) : OrderItem.desc(column));
        }
        return page;
    }

    /**
     * MP {@link IPage} → 框架 {@link PageResult}
     */
    public static <T> PageResult<T> toResult(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }
}
