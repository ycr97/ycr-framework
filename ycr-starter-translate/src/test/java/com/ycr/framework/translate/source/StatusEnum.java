package com.ycr.framework.translate.source;

import com.ycr.framework.core.enums.BaseEnum;

/**
 * 测试用 BaseEnum：编码为 Integer，验证字符串容差比较
 *
 * @author ycr
 */
public enum StatusEnum implements BaseEnum<Integer> {

    ENABLED(1, "启用"),
    DISABLED(0, "禁用");

    private final Integer value;
    private final String description;

    StatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
