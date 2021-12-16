package io.github.interceptor;

import io.github.BgreenProperties;
import io.github.common.Metric;
import io.github.common.MetricType;
import io.github.model.MetricConfig;
import io.github.model.MetricParam;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author maike
 * @date 2021年12月09日 3:40 下午
 */
public class ExceptionInterceptor extends BaseIntercept {

    @Resource
    private BgreenProperties bgreenProperties;

    @Override
    public BgreenProperties getBgreenProperties() {
        return bgreenProperties;
    }

    @Bean
    public AspectJExpressionPointcutAdvisor configurableAdvisor() {
        MetricConfig metricConfig = metricConfigPrepare();
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(metricConfig.getExceptionInterceptPackagePath());
        advisor.setAdvice(new ExceptionAdvice());
        return advisor;
    }

    public class ExceptionAdvice implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            MetricParam param = new MetricParam(MetricType.EXCEPTION_HANDLER.getType(), bgreenProperties.getGroupCode(), bgreenProperties.getAppName());
            param.setMetricType(MetricType.EXCEPTION_HANDLER.getType());
            param.put(Metric.EXCEPTION_METHOD, invocation.getMethod().getName());
            try {
                return invocation.proceed();
            } catch (Throwable ex) {
                param.put(Metric.EXCEPTION_CLASS, ex.getClass().getCanonicalName());
                param.put(Metric.EXCEPTION_MSG, ex.getLocalizedMessage());
                param.put(Metric.EXCEPTION_STACK, Arrays.stream(ex.getStackTrace())
                        .limit(metricConfigPrepare().getExceptionStackLength())
                        .collect(Collectors.toList())
                        .toString());
                reportMetricDataToServer(param);
                throw ex;
            }
        }
    }
}
