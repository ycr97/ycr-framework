package com.ycr.framework.data.mp.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ycr.framework.context.holder.UserContextHolder;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * @author ycr
 */
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContextHolder.getUserId();

        fillIfNull(metaObject, "createTime", now);
        fillIfNull(metaObject, "updateTime", now);
        fillIfNull(metaObject, "createUser", userId);
        fillIfNull(metaObject, "updateUser", userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        fillIfNull(metaObject, "updateTime", LocalDateTime.now());
        fillIfNull(metaObject, "updateUser", UserContextHolder.getUserId());
    }

    private void fillIfNull(MetaObject metaObject, String fieldName, Object fieldValue) {
        if (fieldValue == null) {
            return;
        }
        if (metaObject.hasSetter(fieldName) && getFieldValByName(fieldName, metaObject) == null) {
            setFieldValByName(fieldName, fieldValue, metaObject);
        }
    }
}
