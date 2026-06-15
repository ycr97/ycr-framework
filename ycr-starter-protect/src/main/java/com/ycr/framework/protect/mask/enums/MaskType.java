package com.ycr.framework.protect.mask.enums;

import com.ycr.framework.protect.mask.strategy.MaskStrategy;

/**
 * 内置脱敏类型
 *
 * <p>每个类型即一种 {@link MaskStrategy}。除 {@link #CUSTOM} 外，{@code left/right} 参数被忽略，按固定规则脱敏。</p>
 *
 * @author ycr
 */
public enum MaskType implements MaskStrategy {

    /** 自定义：保留左 {@code left} 位、右 {@code right} 位，中间用脱敏符号替换 */
    CUSTOM {
        @Override
        public String mask(String value, char character, int left, int right) {
            return maskBetween(value, left, value.length() - right, character);
        }
    },

    /** 中文姓名：仅保留第 1 个字，如 张** */
    CHINESE_NAME {
        @Override
        public String mask(String value, char character, int left, int right) {
            return maskBetween(value, 1, value.length(), character);
        }
    },

    /** 手机号：保留前 3 后 4，如 138****5678 */
    MOBILE_PHONE {
        @Override
        public String mask(String value, char character, int left, int right) {
            return maskBetween(value, 3, value.length() - 4, character);
        }
    },

    /** 固定电话：保留前 4 后 2 */
    FIXED_PHONE {
        @Override
        public String mask(String value, char character, int left, int right) {
            return maskBetween(value, 4, value.length() - 2, character);
        }
    },

    /** 身份证号：保留前 6 后 4 */
    ID_CARD {
        @Override
        public String mask(String value, char character, int left, int right) {
            return maskBetween(value, 6, value.length() - 4, character);
        }
    },

    /** 银行卡号：保留前 4 后 4 */
    BANK_CARD {
        @Override
        public String mask(String value, char character, int left, int right) {
            return maskBetween(value, 4, value.length() - 4, character);
        }
    },

    /** 电子邮箱：前缀仅留首字母，@ 及域名不脱敏，如 d****@126.com */
    EMAIL {
        @Override
        public String mask(String value, char character, int left, int right) {
            int at = value.indexOf('@');
            if (at <= 1) {
                return value;
            }
            return maskBetween(value, 1, at, character);
        }
    },

    /** 密码：全部替换为脱敏符号 */
    PASSWORD {
        @Override
        public String mask(String value, char character, int left, int right) {
            return maskBetween(value, 0, value.length(), character);
        }
    };

    /**
     * 将 {@code [start, end)} 区间的字符替换为脱敏符号，越界自动收敛；区间为空则原样返回。
     */
    protected static String maskBetween(String value, int start, int end, char character) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int from = Math.max(0, start);
        int to = Math.min(value.length(), end);
        if (from >= to) {
            return value;
        }
        char[] chars = value.toCharArray();
        for (int i = from; i < to; i++) {
            chars[i] = character;
        }
        return new String(chars);
    }
}
