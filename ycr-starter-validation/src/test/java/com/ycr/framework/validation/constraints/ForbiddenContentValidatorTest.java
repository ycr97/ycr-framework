package com.ycr.framework.validation.constraints;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ForbiddenContentValidator 非法内容检测测试
 *
 * @author ycr
 */
class ForbiddenContentValidatorTest {

    private final ForbiddenContentValidator validator = new ForbiddenContentValidator();

    @Test
    @DisplayName("空值视为合法")
    void shouldMatchExpectedBehavior001() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("   ", null));
        assertTrue(validator.isValid("正常的中文内容 normal text", null));
    }

    @Test
    @DisplayName("检出XSS脚本")
    void shouldMatchExpectedBehavior002() {
        assertFalse(validator.isValid("<script>alert(1)</script>", null));
        assertFalse(validator.isValid("javascript:alert(1)", null));
    }

    @Test
    @DisplayName("检出标签事件属性XSS不被video等标签绕过")
    void shouldMatchExpectedBehavior003() {
        assertFalse(validator.isValid("<video onerror=alert(1)>", null));
        assertFalse(validator.isValid("<video onloadstart=alert(1)>", null));
        assertFalse(validator.isValid("<img src=x onerror=alert(1)>", null));
        assertFalse(validator.isValid("<div onclick=\"alert(1)\">x</div>", null));
    }

    @Test
    @DisplayName("检出SQL注入组合特征")
    void shouldMatchExpectedBehavior004() {
        assertFalse(validator.isValid("1 UNION SELECT password FROM users", null));
        assertFalse(validator.isValid("DROP TABLE orders", null));
        assertFalse(validator.isValid("'; DROP TABLE users--", null));
    }

    @Test
    @DisplayName("普通业务英文文本不被SQL规则误杀")
    void shouldMatchExpectedBehavior005() {
        assertTrue(validator.isValid("please select a city", null));
        assertTrue(validator.isValid("update profile", null));
        assertTrue(validator.isValid("create a new account", null));
    }

    @Test
    @DisplayName("检出爬虫特征")
    void shouldMatchExpectedBehavior006() {
        assertFalse(validator.isValid("curl http://x", null));
        assertFalse(validator.isValid("this is a spider", null));
    }

    @Test
    @DisplayName("含bot子串的普通词不被爬虫规则误杀")
    void shouldMatchExpectedBehavior007() {
        assertTrue(validator.isValid("robot framework", null));
        assertTrue(validator.isValid("abbot street", null));
    }

    @Test
    @DisplayName("检出python代码特征")
    void shouldMatchExpectedBehavior008() {
        assertFalse(validator.isValid("import os", null));
        assertFalse(validator.isValid("eval(payload)", null));
    }
}
