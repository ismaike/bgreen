package io.github.interceptor;

import io.github.Intercept;
import io.github.model.MetricParam;
import io.github.remoting.api.ServerApi;
import io.github.remoting.client.OkHttpClient;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author maike
 * @date 2021年12月09日 3:40 下午
 */
public class ExceptionInterceptor implements Intercept {

    /**
     * TODO 配置需集中管理
     */
    @Value("${exception.pointcut}")
    private String pointcut;

    @Bean
    public AspectJExpressionPointcutAdvisor configurableAdvisor() {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(pointcut);
        advisor.setAdvice(new ExceptionAdvice());
        return advisor;
    }

    @Override
    public ServerApi api() {
        return new OkHttpClient("http://127.0.0.1:8080/upload");
    }

    public class ExceptionAdvice implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            MetricParam param = new MetricParam();
            param.setMetricType(null);
            param.put("methodName", invocation.getMethod().getName());
            try {
                return invocation.proceed();
            } catch (Throwable ex) {
                param.put("exceptionClass", ex.getClass().getCanonicalName());
                param.put("exceptionMsg", ex.getLocalizedMessage());
                // TODO 堆栈长度可配置
                param.put("exceptionStack", Arrays.stream(ex.getStackTrace()).limit(5).collect(Collectors.toList()).toString());
                send(param);
                throw ex;
            }
        }
    }
}
