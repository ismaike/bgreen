package io.github.interceptor;

import io.github.model.MetricConfig;
import io.github.model.MetricParam;
import io.github.model.NetOptions;
import io.github.model.Result;
import io.github.thread.ReportThread;

/**
 * @author maike
 * @date 2021年12月09日 2:37 下午
 */
public interface Intercept {

    /**
     * 发送监控指标数据
     *
     * @param metricParam
     * @return io.github.model.Result<java.lang.String>
     */
    default Result<String> reportMetricDataToServer(MetricParam metricParam) {
        return ReportThread.report(metricParam, netOptionsPrepare(), metricConfigPrepare());
    }

    /**
     * 网络请求超时设置
     *
     * @return
     */
    default NetOptions netOptionsPrepare() {
        return new NetOptions();
    }

    /**
     * 指标配置
     *
     * @return
     */
    MetricConfig metricConfigPrepare();
}
