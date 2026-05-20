package com.ycr.framework.data.permission.autoconfigure;

import com.ycr.framework.data.permission.handler.DataPermissionHandler;
import com.ycr.framework.data.permission.rule.DataPermissionRule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 数据权限自动配置
 *
 * @author ycr
 */
@AutoConfiguration
public class DataPermissionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataPermissionHandler dataPermissionHandler(List<DataPermissionRule> rules) {
        DataPermissionHandler handler = new DataPermissionHandler();
        rules.forEach(handler::addRule);
        return handler;
    }
}
