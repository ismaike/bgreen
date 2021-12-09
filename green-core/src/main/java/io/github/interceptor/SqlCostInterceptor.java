package io.github.interceptor;


import io.github.Intercept;
import io.github.model.MetricParam;
import io.github.remoting.api.ServerApi;
import io.github.remoting.client.OkHttpClient;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author maike
 * @date 2021年12月09日 4:07 下午
 */
@Intercepts(value = {
        @Signature(args = {Statement.class, ResultHandler.class}, method = "query", type = StatementHandler.class),
        @Signature(args = {Statement.class}, method = "update", type = StatementHandler.class),
        @Signature(args = {Statement.class}, method = "batch", type = StatementHandler.class)})
public class SqlCostInterceptor implements Interceptor, Intercept {

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
            String sql = boundSql.getSql();
            Object parameterObject = boundSql.getParameterObject();
            List<ParameterMapping> parameterMappingList = boundSql.getParameterMappings();
            // TODO 是否填充参数可配置，评估性能因素
            // 格式化Sql语句，去除换行符，替换参数
            sql = formatSql(sql, parameterObject, parameterMappingList);
            MetricParam param = new MetricParam();
            param.setMetricType(null);
            param.put("sql", sql);
            param.put("sqlCostTime", String.valueOf(sqlCost));
            send(param);
            System.out.println("SQL：[" + sql + "]执行耗时[" + sqlCost + "ms]");

        }
    }

    @Override
    public ServerApi api() {
        return new OkHttpClient("http://127.0.0.1:8080/uplod");
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private String formatSql(String sql, Object parameterObject, List<ParameterMapping> parameterMappingList) {
        if (!StringUtils.hasText(sql)) {
            return "";
        }
        // 移除换行符
        sql = replaceLineBreak(sql);

        if (parameterObject == null || parameterMappingList == null || parameterMappingList.size() == 0) {
            return sql;
        }

        // 定义一个没有替换过占位符的sql，用于出异常时返回
        String sqlWithoutReplacePlaceholder = sql;
        try {
            Class<?> parameterObjectClass = parameterObject.getClass();

            if (isStrictMap(parameterObjectClass)) {
                DefaultSqlSession.StrictMap<Collection<?>> strictMap = (DefaultSqlSession.StrictMap<Collection<?>>) parameterObject;

                if (isList(strictMap.get("list").getClass())) {
                    sql = handleListParameter(sql, strictMap.get("list"));
                }
            } else if (isMap(parameterObjectClass)) {
                Map<?, ?> paramMap = (Map<?, ?>) parameterObject;
                sql = handleMapParameter(sql, paramMap, parameterMappingList);
            } else {
                sql = handleCommonParameter(sql, parameterMappingList, parameterObjectClass, parameterObject);
            }
        } catch (Exception e) {
            return sqlWithoutReplacePlaceholder;
        }

        return sql;
    }

    /**
     * 处理通用场景
     *
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private String handleCommonParameter(String sql, List<ParameterMapping> parameterMappingList,
                                         Class<?> parameterObjectClass, Object parameterObject) throws Exception {
        for (ParameterMapping parameterMapping : parameterMappingList) {
            String propertyValue = null;
            if (isPrimitiveOrPrimitiveWrapper(parameterObjectClass)) {
                propertyValue = parameterObject.toString();
            } else {
                String propertyName = parameterMapping.getProperty();

                Field field = parameterObjectClass.getDeclaredField(propertyName);
                // 要获取Field中的属性值，这里必须将私有属性的accessible设置为true
                field.setAccessible(true);
                propertyValue = String.valueOf(field.get(parameterObject));
                if (parameterMapping.getJavaType().isAssignableFrom(String.class)) {
                    propertyValue = "\"" + propertyValue + "\"";
                }
            }

            sql = sql.replaceFirst("\\?", propertyValue);
        }

        return sql;
    }

    /**
     * 处理Map场景
     */
    private String handleMapParameter(String sql, Map<?, ?> paramMap, List<ParameterMapping> parameterMappingList) {
        for (ParameterMapping parameterMapping : parameterMappingList) {
            Object propertyName = parameterMapping.getProperty();
            Object propertyValue = paramMap.get(propertyName);
            if (propertyValue != null) {
                if (propertyValue.getClass().isAssignableFrom(String.class)) {
                    propertyValue = "\"" + propertyValue + "\"";
                }

                sql = sql.replaceFirst("\\?", propertyValue.toString());
            }
        }
        return sql;
    }

    /**
     * @param sql
     * @param collection
     * @Description: 处理List场景
     */
    private String handleListParameter(String sql, Collection<?> collection) {
        if (collection != null && collection.size() != 0) {
            for (Object obj : collection) {
                String value = null;
                Class<?> objClass = obj.getClass();

                if (isPrimitiveOrPrimitiveWrapper(objClass)) {
                    value = obj.toString();
                } else if (objClass.isAssignableFrom(String.class)) {
                    value = "\"" + obj + "\"";
                }

                sql = sql.replaceFirst("\\?", value);
            }
        }

        return sql;
    }

    private String replaceLineBreak(String sql) {
        sql = sql.replaceAll("[\\s\n ]+", " ");
        return sql;
    }

    /**
     * 是否基本数据类型或者基本数据类型的包装类
     */
    private boolean isPrimitiveOrPrimitiveWrapper(Class<?> parameterObjectClass) {
        return parameterObjectClass.isPrimitive() || (parameterObjectClass.isAssignableFrom(Byte.class)
                || parameterObjectClass.isAssignableFrom(Short.class)
                || parameterObjectClass.isAssignableFrom(Integer.class)
                || parameterObjectClass.isAssignableFrom(Long.class)
                || parameterObjectClass.isAssignableFrom(Double.class)
                || parameterObjectClass.isAssignableFrom(Float.class)
                || parameterObjectClass.isAssignableFrom(Character.class)
                || parameterObjectClass.isAssignableFrom(Boolean.class));
    }

    /**
     * 是否DefaultSqlSession的内部类StrictMap
     */
    private boolean isStrictMap(Class<?> parameterObjectClass) {
        return parameterObjectClass.isAssignableFrom(DefaultSqlSession.StrictMap.class);
    }

    /**
     * 是否List的实现类
     */
    private boolean isList(Class<?> clazz) {
        Class<?>[] interfaceClasses = clazz.getInterfaces();
        for (Class<?> interfaceClass : interfaceClasses) {
            if (interfaceClass.isAssignableFrom(List.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否Map的实现类
     */
    private boolean isMap(Class<?> parameterObjectClass) {
        Class<?>[] interfaceClasses = parameterObjectClass.getInterfaces();
        for (Class<?> interfaceClass : interfaceClasses) {
            if (interfaceClass.isAssignableFrom(Map.class)) {
                return true;
            }
        }

        return false;
    }

}
