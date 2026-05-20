package com.ycr.framework.id.generator;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void 生成的ID应为正数() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        long id = generator.nextId();
        assertTrue(id > 0);
    }

    @Test
    void 连续生成的ID不重复() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            assertTrue(ids.add(generator.nextId()));
        }
        assertEquals(10000, ids.size());
    }

    @Test
    void 生成的ID递增() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        long prev = generator.nextId();
        for (int i = 0; i < 100; i++) {
            long current = generator.nextId();
            assertTrue(current > prev);
            prev = current;
        }
    }
}
