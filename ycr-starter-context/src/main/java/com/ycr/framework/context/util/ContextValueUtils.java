package com.ycr.framework.context.util;

import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 上下文字段序列化工具。
 *
 * @author ycr
 */
public final class ContextValueUtils {

    private ContextValueUtils() {
    }

    /**
     * 解析逗号分隔字符串，保留原始顺序并过滤空值。
     */
    public static Set<String> parseCommaSeparated(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String item : value.split(",")) {
            String trimmed = item.trim();
            if (StringUtils.hasText(trimmed)) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * 将集合序列化为稳定的逗号分隔字符串。
     */
    public static String joinCommaSeparated(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(","));
    }
}
