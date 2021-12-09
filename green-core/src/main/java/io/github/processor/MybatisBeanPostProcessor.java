package io.github.processor;

import io.github.interceptor.SqlCostInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author maike
 * @date 2021年12月09日 7:37 下午
 */
public class MybatisBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SqlSessionFactory) {
            ((SqlSessionFactory) bean).getConfiguration().addInterceptor(new SqlCostInterceptor());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
