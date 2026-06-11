package com.ycr.framework.core.model;

import java.io.Serializable;

public class R<T> implements Serializable {

    private static final String SUCCESS_CODE = "200";
    private static final String SUCCESS_MSG = "操作成功";

    private String code;
    private String msg;
    private boolean success;
    private Long timestamp;
    private T data;

    public R() {
        this.timestamp = System.currentTimeMillis();
    }

    private R(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = SUCCESS_CODE.equals(code);
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> R<T> ok() {
        return new R<>(SUCCESS_CODE, SUCCESS_MSG, null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS_CODE, msg, data);
    }

    public static <T> R<T> fail(String code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return fail(String.valueOf(code), msg);
    }

    public static <T> R<T> fail(String msg) {
        return fail("500", msg);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
