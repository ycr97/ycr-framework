package com.ycr.framework.protect.mask.strategy;

/**
 * 脱敏策略
 *
 * <p>内置策略见 {@link com.ycr.framework.protect.mask.enums.MaskType}；自定义策略实现本接口，
 * 在 {@code @JsonMask(strategy = Xxx.class)} 引用（可注册为 Spring Bean，亦支持无参构造反射实例化）。</p>
 *
 * @author ycr
 */
public interface MaskStrategy {

    /**
     * 数据脱敏
     *
     * @param value     原始字符串（非空）
     * @param character 脱敏符号
     * @param left      左侧保留位数（仅自定义类型用）
     * @param right     右侧保留位数（仅自定义类型用）
     * @return 脱敏后的字符串
     */
    String mask(String value, char character, int left, int right);
}
