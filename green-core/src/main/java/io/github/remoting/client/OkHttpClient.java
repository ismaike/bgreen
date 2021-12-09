package io.github.remoting.client;

import io.github.model.MetricParam;
import io.github.model.RegistryParam;
import io.github.model.Result;
import io.github.remoting.api.ServerApi;
import io.github.util.GsonUtil;
import io.github.util.RemotingUtil;

/**
 * @author maike
 * @date 2021年12月08日 8:23 下午
 */
public class OkHttpClient implements ServerApi {

    private String url;

    public OkHttpClient(String url) {
        this.url = url;
    }

    @Override
    public Result<String> registry(RegistryParam registryParam) {
        return RemotingUtil.send(url, GsonUtil.toJson(registryParam), String.class, new RemotingUtil.Options());
    }

    @Override
    public Result<String> registryRemove(RegistryParam registryParam) {
        return RemotingUtil.send(url, GsonUtil.toJson(registryParam), String.class, new RemotingUtil.Options());
    }

    @Override
    public Result<String> upload(MetricParam metricParam) {
        return RemotingUtil.send(url, GsonUtil.toJson(metricParam), String.class, new RemotingUtil.Options());
    }
}
