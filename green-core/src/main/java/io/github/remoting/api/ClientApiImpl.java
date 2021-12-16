package io.github.remoting.api;

import io.github.model.MetricConfig;
import io.github.model.Result;
import io.github.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author maike
 * @date 2021年12月16日 11:31 上午
 */
@Slf4j
public class ClientApiImpl implements ClientApi {

    @Override
    public Result<String> beat() {
        return Result.success("I'm fine!");
    }

    @Override
    public Result<Boolean> pushConfig(MetricConfig config) {
        MetricConfig.setCache(config);
        log.info("推送更新配置成功，新的配置为:config={}", GsonUtil.toJson(MetricConfig.getCache()));
        return Result.success(true);
    }
}
