package com.ycr.framework.encrypt.annotation;

import com.ycr.framework.encrypt.enums.EncryptAlgorithm;

import java.lang.annotation.*;

/**
 * 字段加密注解 - 标记需要加解密的实体字段
 *
 * @author ycr
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldEncrypt {

    /** 加密算法 */
    EncryptAlgorithm algorithm() default EncryptAlgorithm.AES;
}
