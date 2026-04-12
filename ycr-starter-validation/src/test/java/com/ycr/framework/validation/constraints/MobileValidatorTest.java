package com.ycr.framework.validation.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobileValidatorTest {

    private final MobileValidator validator = new MobileValidator();

    @Test
    void 合法手机号应通过校验() {
        assertTrue(validator.isValid("13800138000", (ConstraintValidatorContext) null));
        assertTrue(validator.isValid("15912345678", (ConstraintValidatorContext) null));
        assertTrue(validator.isValid("18600001111", (ConstraintValidatorContext) null));
    }

    @Test
    void 非法手机号应失败() {
        assertFalse(validator.isValid("1380013800", (ConstraintValidatorContext) null));
        assertFalse(validator.isValid("23800138000", (ConstraintValidatorContext) null));
        assertFalse(validator.isValid("abcdefghijk", (ConstraintValidatorContext) null));
    }

    @Test
    void null和空串应通过() {
        assertTrue(validator.isValid(null, (ConstraintValidatorContext) null));
        assertTrue(validator.isValid("", (ConstraintValidatorContext) null));
    }
}
