package com.ycr.framework.log.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 日志序列化脱敏管线：审计 body 采集与方法调用日志共用。
 *
 * <p>将任意对象序列化为 JSON，对字段名命中敏感键的值递归脱敏，超长截断。
 * {@code ObjectMapper} 为 null（非 web 应用无 jackson）或序列化异常时静默返回 {@code null}，绝不影响业务。</p>
 *
 * @author ycr
 */
@Slf4j
public class LogJsonSupport {

    /** 脱敏占位 */
    private static final String MASK = "******";
    /** 截断后缀 */
    private static final String TRUNCATED_SUFFIX = "…(truncated)";

    /** 不序列化的噪声/不可序列化类型（按全限定名匹配，避免非 web 运行期加载缺失类）。 */
    private static final Set<String> SKIP_TYPE_NAMES = Set.of(
            "jakarta.servlet.ServletRequest",
            "jakarta.servlet.ServletResponse",
            "org.springframework.web.multipart.MultipartFile",
            "java.io.InputStream",
            "java.io.OutputStream",
            "java.io.Reader",
            "java.io.Writer");

    /** 可空：非 web 应用无 ObjectMapper bean 时为 null。 */
    private final ObjectMapper mapper;
    /** 小写化敏感键。 */
    private final Set<String> sensitiveKeys;

    public LogJsonSupport(ObjectMapper mapper, Set<String> sensitiveKeys) {
        this.mapper = mapper;
        this.sensitiveKeys = sensitiveKeys.stream()
                .map(k -> k.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    /** null 或噪声/不可序列化类型应跳过。 */
    public boolean isSkippable(Object value) {
        return value == null || hasSkipType(value.getClass());
    }

    private boolean hasSkipType(Class<?> type) {
        if (type == null || type == Object.class) {
            return false;
        }
        if (SKIP_TYPE_NAMES.contains(type.getName())) {
            return true;
        }
        for (Class<?> i : type.getInterfaces()) {
            if (hasSkipType(i)) {
                return true;
            }
        }
        return hasSkipType(type.getSuperclass());
    }

    /** 序列化 + 脱敏 + 截断；不可用/异常静默返回 null。 */
    public String serialize(Object value, int maxLength) {
        if (mapper == null || isSkippable(value)) {
            return null;
        }
        try {
            JsonNode node = mapper.valueToTree(value);
            maskInPlace(node);
            return truncate(mapper.writeValueAsString(node), maxLength);
        } catch (Exception e) {
            log.debug("日志序列化失败，已跳过", e);
            return null;
        }
    }

    /** 递归把字段名命中敏感键的值替换为掩码。 */
    private void maskInPlace(JsonNode node) {
        if (node instanceof ObjectNode obj) {
            List<String> names = new ArrayList<>();
            obj.fieldNames().forEachRemaining(names::add);
            for (String name : names) {
                if (sensitiveKeys.contains(name.toLowerCase(Locale.ROOT))) {
                    obj.put(name, MASK);
                } else {
                    maskInPlace(obj.get(name));
                }
            }
        } else if (node instanceof ArrayNode arr) {
            arr.forEach(this::maskInPlace);
        }
    }

    private String truncate(String s, int maxLength) {
        if (s == null || s.length() <= maxLength) {
            return s;
        }
        return s.substring(0, maxLength) + TRUNCATED_SUFFIX;
    }
}
