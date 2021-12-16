package io.github.interceptor;

import io.github.BgreenProperties;
import io.github.common.BgreenEnviroment;
import io.github.model.MetricConfig;
import io.github.model.NetOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * @author maike
 * @date 2021年12月14日 7:52 下午
 */
@Slf4j
public abstract class BaseIntercept implements Intercept, BgreenEnviroment {

    @Override
    public NetOptions netOptionsPrepare() {
        BgreenProperties bgreenProperties = getBgreenProperties();
        if (bgreenProperties != null) {
            return NetOptions.builder()
                    .connectTimeout(bgreenProperties.getNetOptions().getConnectTimeout())
                    .connectTimeoutUnit(bgreenProperties.getNetOptions().getConnectTimeoutUnit())
                    .readTimeout(bgreenProperties.getNetOptions().getReadTimeout())
                    .readTimeoutUnit(bgreenProperties.getNetOptions().getReadTimeoutUnit())
                    .build();
        }
        return Intercept.super.netOptionsPrepare();
    }

    @Override
    public MetricConfig metricConfigPrepare() {
        if (MetricConfig.getCache().getVersion() > 0) {
            return MetricConfig.getCache();
        }
        BgreenProperties bgreenProperties = getBgreenProperties();
        if (bgreenProperties != null) {
            return MetricConfig.builder()
                    .sqlTimeoutThreshold(bgreenProperties.getSqlExecutor().getSqlTimeoutThreshold())
                    .requestTimeoutThreshold(bgreenProperties.getHttpRequest().getRequestTimeoutThreshold())
                    .exceptionExcludedSet(bgreenProperties.getException().getExceptionExcludedSet())
                    .exceptionInterceptPackagePath(bgreenProperties.getException().getExceptionInterceptPackagePath())
                    .exceptionStackLength(bgreenProperties.getException().getExceptionStackLength())
                    .build();
        }
        return MetricConfig.getCache();
    }

    @Override
    public abstract BgreenProperties getBgreenProperties();
}
