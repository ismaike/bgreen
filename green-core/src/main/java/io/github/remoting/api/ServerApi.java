package io.github.remoting.api;

import io.github.model.MetricParam;
import io.github.model.RegistryParam;
import io.github.model.Result;

/**
 * @author maike
 * @date 2021年12月07日 11:05 下午
 */
public interface ServerApi {

    /**
     * registry
     *
     * @param registryParam
     * @return
     */
    Result<String> registry(RegistryParam registryParam);

    /**
     * registry remove
     *
     * @param registryParam
     * @return
     */
    Result<String> registryRemove(RegistryParam registryParam);

    /**
     * metrics data upload
     *
     * @param metricParam
     * @return io.github.model.Result<java.lang.String>
     */
    Result<String> upload(MetricParam metricParam);
}
