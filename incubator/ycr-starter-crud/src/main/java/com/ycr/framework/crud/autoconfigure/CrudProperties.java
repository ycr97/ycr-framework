package com.ycr.framework.crud.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** CRUD 自动端点配置。 */
@Data
@ConfigurationProperties(prefix = "ycr.crud")
public class CrudProperties {

    /** 是否启用继承式 CRUD 自动端点，默认关闭 */
    private boolean enabled = false;
}
