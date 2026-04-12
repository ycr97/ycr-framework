package com.ycr.framework.context.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用上下文
 *
 * @author ycr
 */
@Data
public class AppContext implements Serializable {

    private String appId;

    private String appName;
}
