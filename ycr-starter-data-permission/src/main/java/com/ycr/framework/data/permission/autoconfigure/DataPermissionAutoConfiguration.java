package com.ycr.framework.data.permission.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.ycr.framework.data.permission.handler.DataPermissionHandler;
import com.ycr.framework.data.permission.handler.DataPermissionSqlHandler;
import com.ycr.framework.data.permission.rule.DataPermissionRule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 数据权限自动配置
 *
 * <p>装配链路：{@link DataPermissionRule} 规则 Bean → {@link DataPermissionHandler} 规则注册表
 * → {@link DataPermissionSqlHandler} SQL 适配器 → MyBatis-Plus 数据权限 {@link InnerInterceptor}。</p>
 *
 * <p>这里产出的 {@code InnerInterceptor} 会被 {@code MybatisPlusAutoConfiguration} 通过
 * {@code ObjectProvider<InnerInterceptor>} 自动收集，并织入到分页拦截器之前，从而在查询执行前完成行级权限 SQL 改写。
 * 可通过 {@code ycr.data.permission.enabled=false} 关闭整条数据权限链路。</p>
 *
 * @author ycr
 */
@AutoConfiguration
public class DataPermissionAutoConfiguration {

    /**
     * 汇总容器内所有数据权限规则，构建规则注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public DataPermissionHandler dataPermissionHandler(List<DataPermissionRule> rules) {
        DataPermissionHandler handler = new DataPermissionHandler();
        rules.forEach(handler::addRule);
        return handler;
    }

    /**
     * 将框架规则体系适配为 MyBatis-Plus 的多表数据权限处理器
     */
    @Bean
    @ConditionalOnMissingBean(MultiDataPermissionHandler.class)
    public MultiDataPermissionHandler dataPermissionSqlHandler(DataPermissionHandler dataPermissionHandler) {
        return new DataPermissionSqlHandler(dataPermissionHandler);
    }

    /**
     * 注册 MyBatis-Plus 数据权限内部拦截器，承担真正的 SQL 解析与 WHERE 条件改写
     */
    @Bean
    @ConditionalOnMissingBean(name = "dataPermissionInnerInterceptor")
    @ConditionalOnProperty(prefix = "ycr.data.permission", name = "enabled", havingValue = "true", matchIfMissing = true)
    public InnerInterceptor dataPermissionInnerInterceptor(MultiDataPermissionHandler dataPermissionSqlHandler) {
        return new com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor(dataPermissionSqlHandler);
    }
}
