package com.ycr.framework.encrypt.annotation;

import com.ycr.framework.encrypt.enums.EncryptAlgorithm;

import java.lang.annotation.*;

/**
 * 字段加密标记注解。
 *
 * @deprecated 当前运行时不扫描该注解，请显式使用 MyBatis {@code EncryptTypeHandler}。
 *
 * @author ycr
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated(since = "0.9.0-RC4", forRemoval = false)
public @interface FieldEncrypt {

    /** 加密算法 */
    EncryptAlgorithm algorithm() default EncryptAlgorithm.AES;
}
