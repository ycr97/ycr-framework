package com.ycr.framework.json.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycr.framework.core.autoconfigure.CoreAutoConfiguration;
import com.ycr.framework.core.enums.BaseEnum;
import com.ycr.framework.core.util.SpringContextHolder;
import com.ycr.framework.json.util.JsonUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class,
                    CoreAutoConfiguration.class,
                    JacksonAutoConfiguration.class
            ));

    @AfterEach
    void resetStatics() throws Exception {
        Field defaultMapperField = JsonUtils.class.getDeclaredField("DEFAULT_OBJECT_MAPPER");
        defaultMapperField.setAccessible(true);
        JsonUtils.setObjectMapper((ObjectMapper) defaultMapperField.get(null));

        Field contextField = SpringContextHolder.class.getDeclaredField("context");
        contextField.setAccessible(true);
        contextField.set(null, null);
    }

    @Test
    void 应使用SpringObjectMapper覆盖JsonUtils默认Mapper并尊重大数字开关() {
        contextRunner.withPropertyValues("ycr.json.big-number-to-string=false")
                .run(context -> {
                    Field defaultMapperField = JsonUtils.class.getDeclaredField("DEFAULT_OBJECT_MAPPER");
                    defaultMapperField.setAccessible(true);
                    JsonUtils.setObjectMapper((ObjectMapper) defaultMapperField.get(null));

                    String json = JsonUtils.toJson(Map.of("value", 9007199254740992L));
                    assertEquals("{\"value\":9007199254740992}", json);
                });
    }

    @Test
    void 应支持BaseEnum序列化与反序列化() throws Exception {
        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            String json = objectMapper.writeValueAsString(new EnumHolder(TestStatus.ENABLED));
            assertEquals("{\"status\":1}", json);

            EnumHolder holder = objectMapper.readValue("{\"status\":1}", EnumHolder.class);
            assertEquals(TestStatus.ENABLED, holder.status);
        });
    }

    enum TestStatus implements BaseEnum<Integer> {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用");

        private final Integer value;
        private final String description;

        TestStatus(Integer value, String description) {
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

    static class EnumHolder {

        public TestStatus status;

        EnumHolder() {
        }

        EnumHolder(TestStatus status) {
            this.status = status;
        }
    }
}
