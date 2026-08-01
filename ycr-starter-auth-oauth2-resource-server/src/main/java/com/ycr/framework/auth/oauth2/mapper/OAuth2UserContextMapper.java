package com.ycr.framework.auth.oauth2.mapper;

import com.ycr.framework.context.model.UserContext;

import java.util.Map;

/**
 * OAuth2 claims 到 YCR UserContext 的最小映射 SPI。
 *
 * @author ycr
 */
public interface OAuth2UserContextMapper {

    UserContext map(Map<String, Object> claims);
}
