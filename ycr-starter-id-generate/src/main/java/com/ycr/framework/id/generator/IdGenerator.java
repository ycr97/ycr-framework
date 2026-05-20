package com.ycr.framework.id.generator;

/**
 * ID 生成器接口
 *
 * @author ycr
 */
public interface IdGenerator {

    /**
     * 生成下一个 ID
     */
    long nextId();

    /**
     * 生成下一个字符串 ID
     */
    default String nextIdStr() {
        return String.valueOf(nextId());
    }
}
