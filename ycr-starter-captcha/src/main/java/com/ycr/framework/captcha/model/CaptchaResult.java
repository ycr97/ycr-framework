package com.ycr.framework.captcha.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 验证码生成结果
 *
 * @author ycr
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResult implements Serializable {

    /** 验证码标识，校验时随用户输入一并回传 */
    private String id;

    /** 图片 Base64（data URI，形如 {@code data:image/png;base64,...}），前端可直接作为 img src */
    private String imageBase64;
}
