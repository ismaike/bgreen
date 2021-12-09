package io.github;

import io.github.model.MetricParam;
import io.github.model.Result;
import io.github.remoting.api.ServerApi;

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
    default Result<String> send(MetricParam metricParam) {
        return api().upload(metricParam);
    }

    /**
     * 获取 api 的实现类
     *
     * @return io.github.remoting.client.OkHttpClient
     */
    ServerApi api();
}
