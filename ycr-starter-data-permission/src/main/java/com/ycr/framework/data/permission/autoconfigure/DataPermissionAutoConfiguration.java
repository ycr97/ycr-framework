package com.ycr.framework.data.permission.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.ycr.framework.data.permission.aspect.DataPermissionAspect;
import com.ycr.framework.data.permission.handler.DataPermissionHandler;
import com.ycr.framework.data.permission.handler.DataPermissionSqlHandler;
import com.ycr.framework.data.permission.rule.DataPermissionRule;
import com.ycr.framework.data.permission.scope.CommandTypeResolver;
import com.ycr.framework.data.permission.scope.DataScope;
import com.ycr.framework.data.permission.scope.DataScopeClearFilter;
import com.ycr.framework.data.permission.scope.DataScopeResolver;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 数据权限自动配置。
 *
 * <p>装配链路：{@link DataPermissionRule} 规则 + {@link DataScopeResolver} 取数
 * → {@link DataPermissionHandler} 合并引擎 → {@link DataPermissionSqlHandler} MP 适配器
 * → MyBatis-Plus 数据权限 {@link InnerInterceptor}。缺省 resolver 返回空范围（受治理表 fail-closed），
 * 生产须由 L2 覆盖。{@code ycr.data.permission.enabled=false} 关闭整条链路。</p>
 *
 * @author ycr
 */
@AutoConfiguration
@EnableConfigurationProperties(DataPermissionProperties.class)
public class DataPermissionAutoConfiguration {

    /** 缺省空范围 resolver：未提供时受治理表一律 fail-closed，提醒补 resolver。 */
    @Bean
    @ConditionalOnMissingBean
    public DataScopeResolver dataScopeResolver() {
        return DataScope::empty;
    }

    /** 默认按 MyBatis Configuration 解析语句类型；惰性取 SqlSessionFactory 以打破构造环。 */
    @Bean
    @ConditionalOnMissingBean
    public CommandTypeResolver commandTypeResolver(ObjectProvider<SqlSessionFactory> sqlSessionFactory) {
        return mappedStatementId -> {
            SqlSessionFactory factory = sqlSessionFactory.getIfAvailable();
            if (factory == null || !factory.getConfiguration().hasStatement(mappedStatementId, false)) {
                return SqlCommandType.UNKNOWN;
            }
            return factory.getConfiguration().getMappedStatement(mappedStatementId, false).getSqlCommandType();
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public DataPermissionHandler dataPermissionHandler(List<DataPermissionRule> rules) {
        DataPermissionHandler handler = new DataPermissionHandler();
        rules.forEach(handler::addRule);
        return handler;
    }

    @Bean
    @ConditionalOnMissingBean(MultiDataPermissionHandler.class)
    public MultiDataPermissionHandler dataPermissionSqlHandler(DataPermissionHandler handler,
                                                               DataScopeResolver resolver,
                                                               CommandTypeResolver commandTypeResolver,
                                                               DataPermissionProperties properties) {
        return new DataPermissionSqlHandler(handler, resolver, commandTypeResolver,
                properties.isLogAppliedConditions());
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataPermissionInnerInterceptor")
    @ConditionalOnProperty(prefix = "ycr.data.permission", name = "enabled", havingValue = "true", matchIfMissing = true)
    public InnerInterceptor dataPermissionInnerInterceptor(MultiDataPermissionHandler dataPermissionSqlHandler) {
        return new DataPermissionInterceptor(dataPermissionSqlHandler);
    }

    /** 请求结束清理数据范围缓存，仅 Servlet 应用装配。 */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
    public DataScopeClearFilter dataScopeClearFilter() {
        return new DataScopeClearFilter();
    }

    /**
     * 数据权限注解切面，支持 {@code @DataPermission} / {@code @DataPermissionIgnore} 方法级开关。
     * 仅在引入了 AOP（AspectJ）时装配。
     */
    @Bean
    @ConditionalOnClass(Aspect.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ycr.data.permission", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DataPermissionAspect dataPermissionAspect() {
        return new DataPermissionAspect();
    }
}
