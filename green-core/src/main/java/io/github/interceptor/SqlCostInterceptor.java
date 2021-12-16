package io.github.interceptor;


import io.github.BgreenProperties;
import io.github.common.Metric;
import io.github.common.MetricType;
import io.github.model.MetricParam;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author maike
 * @date 2021年12月09日 4:07 下午
 */
@Intercepts(value = {
        @Signature(args = {Statement.class, ResultHandler.class}, method = "query", type = StatementHandler.class),
        @Signature(args = {Statement.class}, method = "update", type = StatementHandler.class),
        @Signature(args = {Statement.class}, method = "batch", type = StatementHandler.class)})
public class SqlCostInterceptor extends BaseIntercept implements Interceptor {

    @Resource
    private BgreenProperties bgreenProperties;

    @Override
    public BgreenProperties getBgreenProperties() {
        return bgreenProperties;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();

        long startTime = System.currentTimeMillis();
        StatementHandler statementHandler = (StatementHandler) target;
        try {
            return invocation.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long sqlCost = endTime - startTime;
            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = formatSql(boundSql.getSql());
            MetricParam param = new MetricParam(MetricType.SQL_EXECUTOR.getType(), bgreenProperties.getGroupCode(), bgreenProperties.getAppName());
            param.setMetricType(MetricType.SQL_EXECUTOR.getType());
            param.put(Metric.SQL_TEXT, sql);
            param.put(Metric.SQL_COST_TIME, String.valueOf(sqlCost));
            reportMetricDataToServer(param);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private String formatSql(String sql) {
        if (!StringUtils.hasText(sql)) {
            return "";
        }
        return replaceLineBreak(sql);
    }

    private String replaceLineBreak(String sql) {
        sql = sql.replaceAll("[\\s\n ]+", " ");
        return sql;
    }
}
