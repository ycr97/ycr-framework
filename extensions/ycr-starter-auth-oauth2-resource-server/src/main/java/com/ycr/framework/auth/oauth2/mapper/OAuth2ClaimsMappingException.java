package com.ycr.framework.auth.oauth2.mapper;

/**
 * OAuth2 claims 无法形成有效 YCR 身份时抛出的异常。
 *
 * @author ycr
 */
public class OAuth2ClaimsMappingException extends RuntimeException {

    public OAuth2ClaimsMappingException(String message) {
        super(message);
    }

    public OAuth2ClaimsMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
