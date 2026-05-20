package com.ycr.framework.data.permission.interceptor;

import com.ycr.framework.data.permission.handler.DataPermissionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据权限 MyBatis-Plus 拦截器
 * 在 SQL 执行前自动追加数据权限过滤条件
 *
 * TODO: 当前为骨架实现，完整的 InnerInterceptor 集成待后续迭代补充
 *
 * @author ycr
 */
@Slf4j
@RequiredArgsConstructor
public class DataPermissionInterceptor {

    private final DataPermissionHandler handler;
}
