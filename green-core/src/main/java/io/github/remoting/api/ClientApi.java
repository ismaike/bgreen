package io.github.remoting.api;


import io.github.model.MetricConfig;
import io.github.model.Result;

/**
 * @author maike
 * @date 2021年12月07日 10:44 下午
 */
public interface ClientApi {
    /**
     * beat
     *
     * @return
     */
    Result<String> beat();

    /**
     * TODO 远程配置推送
     *
     * @return
     */
    Result<Boolean> pushConfig(MetricConfig config);
}
