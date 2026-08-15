package com.ycr.framework.data.permission.exception;

/**
 * 数据权限范围解析失败异常：resolver 抛错时 fail-closed 且 fail-loud，中止查询。
 *
 * @author ycr
 */
public class DataPermissionException extends RuntimeException {

    public DataPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
