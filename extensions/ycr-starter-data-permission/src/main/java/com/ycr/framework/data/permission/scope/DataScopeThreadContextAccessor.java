package com.ycr.framework.data.permission.scope;

import com.ycr.framework.context.propagation.ThreadContextAccessor;

/** 将数据权限请求级缓存纳入统一线程上下文传播。 */
public class DataScopeThreadContextAccessor implements ThreadContextAccessor {

    @Override
    public Object capture() {
        return DataScopeContext.capture();
    }

    @Override
    public void restore(Object captured) {
        DataScopeContext.restore((DataScope) captured);
    }
}
