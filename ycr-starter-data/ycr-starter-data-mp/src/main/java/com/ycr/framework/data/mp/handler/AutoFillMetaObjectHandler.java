package com.ycr.framework.data.mp.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
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

        strictInsert(metaObject, "createTime", LocalDateTime.class, now);
        strictInsert(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsert(metaObject, "createUser", Long.class, userId);
        strictInsert(metaObject, "updateUser", Long.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdate(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        strictUpdate(metaObject, "updateUser", Long.class, UserContextHolder.getUserId());
    }

    private <T> void strictInsert(MetaObject metaObject, String fieldName, Class<T> fieldType, T fieldValue) {
        if (fieldValue == null) {
            return;
        }
        if (hasTableInfo(metaObject)) {
            strictInsertFill(metaObject, fieldName, fieldType, fieldValue);
            return;
        }
        if (getFieldValByName(fieldName, metaObject) == null) {
            setFieldValByName(fieldName, fieldValue, metaObject);
        }
    }

    private <T> void strictUpdate(MetaObject metaObject, String fieldName, Class<T> fieldType, T fieldValue) {
        if (fieldValue == null) {
            return;
        }
        if (hasTableInfo(metaObject)) {
            strictUpdateFill(metaObject, fieldName, fieldType, fieldValue);
            return;
        }
        if (getFieldValByName(fieldName, metaObject) == null) {
            setFieldValByName(fieldName, fieldValue, metaObject);
        }
    }

    private boolean hasTableInfo(MetaObject metaObject) {
        Object originalObject = metaObject.getOriginalObject();
        return originalObject != null && TableInfoHelper.getTableInfo(originalObject.getClass()) != null;
    }
}
