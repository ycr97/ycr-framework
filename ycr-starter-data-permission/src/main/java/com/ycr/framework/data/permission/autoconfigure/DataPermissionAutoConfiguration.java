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
import com.ycr.framework.data.permission.scope.DataScopeThreadContextAccessor;
import com.ycr.framework.context.propagation.ThreadContextAccessor;
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
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
@ConditionalOnProperty(prefix = "ycr.data.permission", name = "enabled", havingValue = "true")
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
    public DataPermissionHandler dataPermissionHandler(List<DataPermissionRule> rules,
                                                       DataPermissionProperties properties) {
        validateGovernedTables(rules, properties);
        DataPermissionHandler handler = new DataPermissionHandler(properties.getGovernedTables());
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

    @Bean
    @ConditionalOnMissingBean(name = "dataScopeThreadContextAccessor")
    public ThreadContextAccessor dataScopeThreadContextAccessor() {
        return new DataScopeThreadContextAccessor();
    }

    /**
     * 数据权限注解切面，支持 {@code @DataPermission} / {@code @DataPermissionIgnore} 方法级开关。
     * 仅在引入了 AOP（AspectJ）时装配。
     */
    @Bean
    @ConditionalOnClass(Aspect.class)
    @ConditionalOnMissingBean
    public DataPermissionAspect dataPermissionAspect() {
        return new DataPermissionAspect();
    }

    private void validateGovernedTables(List<DataPermissionRule> rules, DataPermissionProperties properties) {
        Set<String> governed = properties.getGovernedTables().stream()
                .map(this::normalizeTable)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());
        if (governed.isEmpty()) {
            throw new IllegalStateException(
                    "ycr.data.permission.governed-tables 必须在启用数据权限时显式配置");
        }
        Set<String> ruled = rules.stream()
                .map(DataPermissionRule::table)
                .map(this::normalizeTable)
                .collect(Collectors.toSet());
        Set<String> missing = governed.stream().filter(table -> !ruled.contains(table)).collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            throw new IllegalStateException("受治理表缺少 DataPermissionRule: " + missing);
        }
        Set<String> undeclared = ruled.stream().filter(table -> !governed.contains(table)).collect(Collectors.toSet());
        if (!undeclared.isEmpty()) {
            throw new IllegalStateException("DataPermissionRule 未声明对应受治理表: " + undeclared);
        }
    }

    private String normalizeTable(String table) {
        return table == null ? "" : table.replace("`", "").replace("\"", "")
                .toLowerCase(Locale.ROOT).trim();
    }
}
