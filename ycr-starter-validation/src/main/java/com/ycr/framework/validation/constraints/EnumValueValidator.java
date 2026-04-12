package com.ycr.framework.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 枚举值校验器
 *
 * @author ycr
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {

    private Set<Integer> intValues;
    private Set<String> strValues;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        intValues = Arrays.stream(constraintAnnotation.intValues()).boxed().collect(Collectors.toSet());
        strValues = Arrays.stream(constraintAnnotation.strValues()).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof Integer integer) {
            return intValues.isEmpty() || intValues.contains(integer);
        }
        if (value instanceof String string) {
            return strValues.isEmpty() || strValues.contains(string);
        }
        return false;
    }
}
