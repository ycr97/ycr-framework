package com.ycr.framework.validation.constraints;

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
    void 空值视为合法() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("   ", null));
        assertTrue(validator.isValid("正常的中文内容 normal text", null));
    }

    @Test
    void 检出XSS脚本() {
        assertFalse(validator.isValid("<script>alert(1)</script>", null));
        assertFalse(validator.isValid("javascript:alert(1)", null));
    }

    @Test
    void 检出SQL关键字() {
        assertFalse(validator.isValid("1 UNION SELECT password FROM users", null));
        assertFalse(validator.isValid("DROP TABLE orders", null));
    }

    @Test
    void 检出爬虫特征() {
        assertFalse(validator.isValid("curl http://x", null));
        assertFalse(validator.isValid("this is a spider", null));
    }

    @Test
    void 检出python代码特征() {
        assertFalse(validator.isValid("import os", null));
        assertFalse(validator.isValid("eval(payload)", null));
    }
}
