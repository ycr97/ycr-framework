package com.ycr.framework.data.permission.scope;

/**
 * 数据范围解析 SPI：由 L2（公司 common）实现，按当前主体取各维度可见值。
 *
 * <p>匿名/无登录场景应返回「适用且空」的维度（→ Deny），而非缺键，避免 fail-open；
 * 抛异常视为系统级失败，框架将 fail-closed 中止本次查询。</p>
 *
 * @author ycr
 */
public interface DataScopeResolver {

    DataScope resolve();
}
