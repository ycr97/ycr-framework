package com.ycr.framework.validation.constraints;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumValueValidatorTest {

    private final EnumValueValidator validator = new EnumValueValidator();

    @Test
    void 整数值应按intValues校验() {
        validator.initialize(enumValue(new int[]{1, 2}, new String[0]));

        assertTrue(validator.isValid(1, (ConstraintValidatorContext) null));
        assertTrue(validator.isValid(2, (ConstraintValidatorContext) null));
        assertFalse(validator.isValid(3, (ConstraintValidatorContext) null));
    }

    @Test
    void 字符串值应按strValues校验() {
        validator.initialize(enumValue(new int[0], new String[]{"A", "B"}));

        assertTrue(validator.isValid("A", (ConstraintValidatorContext) null));
        assertTrue(validator.isValid("B", (ConstraintValidatorContext) null));
        assertFalse(validator.isValid("C", (ConstraintValidatorContext) null));
    }

    @Test
    void null和其他类型应按约定返回() {
        validator.initialize(enumValue(new int[]{1}, new String[]{"A"}));

        assertTrue(validator.isValid(null, (ConstraintValidatorContext) null));
        assertFalse(validator.isValid(1.5d, (ConstraintValidatorContext) null));
    }

    private EnumValue enumValue(int[] intValues, String[] strValues) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                return switch (name) {
                    case "intValues" -> intValues;
                    case "strValues" -> strValues;
                    case "message" -> "{com.ycr.framework.validation.constraints.EnumValue.message}";
                    case "groups" -> new Class<?>[0];
                    case "payload" -> new Class[0];
                    case "annotationType" -> EnumValue.class;
                    default -> method.getDefaultValue();
                };
            }
        };

        return (EnumValue) Proxy.newProxyInstance(
                EnumValue.class.getClassLoader(),
                new Class<?>[]{EnumValue.class, Annotation.class},
                handler
        );
    }
}
