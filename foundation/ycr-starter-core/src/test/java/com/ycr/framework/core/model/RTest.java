package com.ycr.framework.core.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RTest {

    @Test
    @DisplayName("ok_无参_应返回成功响应")
    void shouldMatchExpectedBehavior001() {
        R<Void> response = R.ok();

        assertEquals("200", response.getCode());
        assertEquals("操作成功", response.getMsg());
        assertTrue(response.isSuccess());
        assertNotNull(response.getTimestamp());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("ok_带数据_应包含数据")
    void shouldMatchExpectedBehavior002() {
        R<String> response = R.ok("hello");

        assertEquals("200", response.getCode());
        assertEquals("hello", response.getData());
        assertTrue(response.isSuccess());
    }

    @Test
    @DisplayName("fail_应返回失败响应")
    void shouldMatchExpectedBehavior003() {
        R<Void> response = R.fail(500, "系统错误");

        assertEquals("500", response.getCode());
        assertEquals("系统错误", response.getMsg());
        assertFalse(response.isSuccess());
    }

    @Test
    @DisplayName("fail_从ErrorCode构建")
    void shouldMatchExpectedBehavior004() {
        R<Void> response = R.fail(400, "参数错误");

        assertEquals("400", response.getCode());
        assertEquals("参数错误", response.getMsg());
        assertFalse(response.isSuccess());
    }
}
